package com.gcplot;

import com.dumbster.smtp.SimpleSmtpServer;
import com.gcplot.bootstrap.Bootstrap;
import com.gcplot.cassandra.CassandraConnector;
import com.gcplot.cassandra.Server;
import com.gcplot.commons.ConfigProperty;
import com.gcplot.commons.FileUtils;
import com.gcplot.commons.Utils;
import com.gcplot.commons.serialization.JsonSerializer;
import com.gcplot.configuration.ConfigurationManager;
import com.gcplot.messages.RegisterRequest;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public abstract class IntegrationTest {

    protected SimpleSmtpServer smtpServer;
    protected static Server cassandraServer = new Server();
    private Bootstrap bootstrap;
    public Bootstrap getBoot() {
        return bootstrap;
    }
    public ApplicationContext getApplicationContext() {
        return bootstrap.getApplicationContext();
    }
    public <T> T getBean(Class<T> bean) {
        return getApplicationContext().getBean(bean);
    }
    private File tempDir;
    public File getTempDir() {
        return tempDir;
    }
    private Utils.Port port;
    public int getPort() {
        return port.value;
    }

    private String dbName;
    protected ODatabaseDocumentTx database;

    @BeforeClass
    public static void beforeClass() throws Exception {
        cassandraServer.start();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        cassandraServer.stop();
    }

    @Before
    public void setUp() throws Exception {
        createCassandraScheme();
        smtpServer = SimpleSmtpServer.start(SimpleSmtpServer.AUTO_SMTP_PORT);
        dbName = Utils.getRandomIdentifier();
        database = new ODatabaseDocumentTx("memory:" + dbName).create();

        makeTestPropertiesFile();
        bootstrap = new Bootstrap();
        bootstrap.hang = false;
        bootstrap.configDir = tempDir.getAbsolutePath();
        intercept();
        bootstrap.run();
        client = getApplicationContext().getBean(Vertx.class).createHttpClient();
        ConfigurationManager cm = getApplicationContext().getBean(ConfigurationManager.class);
        cm.putProperty(ConfigProperty.API_HOST, LOCALHOST + ":" + port.value);
        cm.putProperty(ConfigProperty.PUBLIC_HOST, LOCALHOST);
        cm.putProperty(ConfigProperty.EMAIL_USE_SSL, false);
        cm.putProperty(ConfigProperty.EMAIL_SMTP_PORT, smtpServer.getPort());
        cm.putProperty(ConfigProperty.EMAIL_HOST_NAME, LOCALHOST);
        cm.putProperty(ConfigProperty.EMAIL_AUTH, false);
        cm.putProperty(ConfigProperty.EMAIL_CONFIRM_TEMPLATE, "");
    }

    @After
    public void tearDown() throws Exception {
        try {
            ((ConfigurableApplicationContext) getApplicationContext()).close();
        } finally {
            try {
                FileUtils.delete(tempDir);
            } catch (Throwable ignored) {
            }
            port.unlock();
            try {
                new ODatabaseDocumentTx("memory:" + dbName).open("admin", "admin").drop();
            } catch (Throwable t) {
                LOG.error(t.getMessage(), t);
            } finally {
                try {
                    client.close();
                } catch (Throwable ignored) {
                }
            }
            try {
                smtpServer.close();
            } catch (Throwable t) {
                LOG.error(t.getMessage(), t);
            }
            try {
                cassandraServer.clean();
            } catch (Throwable t) {
                LOG.error(t.getMessage(), t);
            }
        }
    }

    protected void createCassandraScheme() throws IOException, URISyntaxException {
        CassandraConnector connector = new CassandraConnector();
        connector.setHosts(new String[]{EmbeddedCassandraServerHelper.getHost()});
        connector.setPort(cassandraServer.getNativePort());
        connector.setKeyspace(null);
        connector.init();
        String q = readFile(new File(Server.class.getClassLoader().getResource("cassandra-scheme.sql").toURI()).getAbsolutePath(), Charsets.UTF_8);
        for (String qq : q.split(";")) {
            if (!Strings.isNullOrEmpty(qq.trim())) {
                connector.session().execute(qq.trim());
            }
        }
        connector.destroy();
    }

    protected void makeTestPropertiesFile() throws IOException {
        tempDir = Files.createTempDir();
        port = Utils.getFreePorts(1)[0];
        StringBuilder sb = new StringBuilder();
        sb.append("bootstrap.server.port=").append(port.value).append('\n');
        sb.append("bootstrap.server.host=").append(LOCALHOST).append('\n');
        sb.append("orientdb.connection.string=memory:").append(dbName).append('\n');
        sb.append("cassandra.port=").append(cassandraServer.getNativePort()).append('\n');
        Files.write(sb.toString(), new File(tempDir, "gcplot.properties"), Charset.forName("UTF-8"));
    }

    public void intercept() {
    }

    protected JsonObject delete(String path, String token) throws Exception {
        return delete(withToken(path, token), -1);
    }

    protected JsonObject delete(String path, String token, long expectedError) throws Exception {
        return delete(withToken(path, token), expectedError);
    }

    protected JsonObject delete(String path, long expectedError) throws Exception {
        final CountDownLatch l = new CountDownLatch(1);
        final JsonObject[] jo = new JsonObject[1];
        LOG.info("DELETE {}", path);
        if (path.startsWith("http")) {
            client.deleteAbs(path, r -> r.bodyHandler(b -> handleResponse(jo, a -> true, expectedError, l, b))).end();
        } else {
            client.delete(port.value, LOCALHOST, path, r -> r.bodyHandler(b -> handleResponse(jo, a -> true, expectedError, l, b))).end();
        }
        Assert.assertTrue(l.await(3, TimeUnit.SECONDS));
        return jo[0];
    }

    protected JsonObject get(String path, String token, long expectedError) throws Exception {
        return get(withToken(path, token), expectedError);
    }

    protected JsonObject get(String path, long expectedError) throws Exception {
        return get(path, a -> true, expectedError);
    }

    protected JsonObject get(String path, String token, Predicate<JsonObject> test) throws Exception {
        return get(withToken(path, token), test);
    }

    protected JsonObject get(String path, Predicate<JsonObject> test) throws Exception {
        return get(path, test, -1);
    }

    protected JsonObject get(String path, Predicate<JsonObject> test, long expectedError) throws Exception {
        final CountDownLatch l = new CountDownLatch(1);
        final JsonObject[] jo = new JsonObject[1];
        LOG.info("GET {}", path);
        if (path.startsWith("http")) {
            client.getAbs(path, r -> r.bodyHandler(b -> handleResponse(jo, test, expectedError, l, b))).end();
        } else {
            client.get(port.value, LOCALHOST, path, r -> r.bodyHandler(b -> handleResponse(jo, test, expectedError, l, b))).end();
        }
        Assert.assertTrue(l.await(3, TimeUnit.SECONDS));
        return jo[0];
    }

    protected JsonObject post(String path, String message, String token, long expectedError) throws Exception {
        return post(withToken(path, token), message, expectedError);
    }

    protected JsonObject post(String path, Object message, String token, long expectedError) throws Exception {
        return post(withToken(path, token), message, expectedError);
    }

    protected JsonObject post(String path, Object message, String token, Predicate<JsonObject> test) throws Exception {
        return post(withToken(path, token), message, test);
    }

    protected JsonObject post(String path, String message, long expectedError) throws Exception {
        return post(path, message, a -> true, expectedError);
    }

    protected JsonObject post(String path, Object message, long expectedError) throws Exception {
        return post(path, JsonSerializer.serialize(message), a -> true, expectedError);
    }

    protected JsonObject post(String path, Object message, Predicate<JsonObject> test) throws Exception {
        return post(path, JsonSerializer.serialize(message), test, -1);
    }

    protected JsonObject post(String path, String message, Predicate<JsonObject> test, long expectedError) throws Exception {
        final CountDownLatch l = new CountDownLatch(1);
        final JsonObject[] jo = new JsonObject[1];
        LOG.info("POST {} BODY {}", path, message);
        client.post(port.value, LOCALHOST, path, r -> r.bodyHandler(b -> handleResponse(jo, test, expectedError, l, b))).end(message);
        Assert.assertTrue(l.await(3, TimeUnit.SECONDS));
        return jo[0];
    }

    protected JsonObject login(RegisterRequest request) throws Exception {
        StringBuilder sb = new StringBuilder();
        get("/user/login?login=" + request.username + "&password=" + request.password, jo -> {
            sb.append(jo);
            return jo.containsKey("result");
        });
        return new JsonObject(sb.toString()).getJsonObject("result");
    }

    protected void handleResponse(JsonObject[] jr, Predicate<JsonObject> test, long expectedError, CountDownLatch l, Buffer b) {
        JsonObject jo = new JsonObject(new String(b.getBytes()));
        jr[0] = jo;
        LOG.info("Response: {}", jo);
        if (jo.containsKey("error") && expectedError == -1) {
            LOG.error("ERROR: [code={}, message={}];", jo.getLong("error"), jo.getString("message"));
        } else if (expectedError != -1) {
            if (jo.containsKey("error") && jo.getLong("error").equals(expectedError)) {
                l.countDown();
            } else {
                LOG.error("Expected error, but no error was provided, instead: " + jo);
            }
        } else if (test.test(jo)) {
            l.countDown();
        }
    }

    protected static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = java.nio.file.Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    protected JsonObject r(JsonObject j) {
        return j.getJsonObject("result");
    }

    protected JsonArray ra(JsonObject j) {
        return j.getJsonArray("result");
    }

    private String withToken(String path, String token) {
        return path.contains("?") ? path + "&token=" + token : path + "?token=" + token;
    }

    protected HttpClient client;
    protected static final String LOCALHOST = "127.0.0.1";
    protected static final Logger LOG = LoggerFactory.getLogger(IntegrationTest.class);
}
