package com.gcplot;

import com.dumbster.smtp.SimpleSmtpServer;
import com.gcplot.bootstrap.Bootstrap;
import com.gcplot.commons.ConfigProperty;
import com.gcplot.commons.FileUtils;
import com.gcplot.commons.Utils;
import com.gcplot.commons.serialization.JsonSerializer;
import com.gcplot.configuration.ConfigurationManager;
import com.gcplot.web.Dispatcher;
import com.google.common.io.Files;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public abstract class IntegrationTest {

    protected SimpleSmtpServer smtpServer;
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
    private int port;
    public int getPort() {
        return port;
    }

    private String dbName;
    protected ODatabaseDocumentTx database;

    @Before
    public void setUp() throws Exception {
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
        cm.putProperty(ConfigProperty.API_HOST, LOCALHOST + ":" + port);
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
            FileUtils.delete(tempDir);
        } catch (Throwable ignored) {
        }
        try {
            new ODatabaseDocumentTx("memory:" + dbName).open("admin", "admin").drop();
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        } finally {
            try {
                client.close();
            } catch (Throwable ignored) {}
        }
        try {
            smtpServer.close();
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
        ((ConfigurableApplicationContext)getApplicationContext()).close();
    }

    protected void makeTestPropertiesFile() throws IOException {
        tempDir = Files.createTempDir();
        port = Utils.getFreePorts(1)[0];
        StringBuilder sb = new StringBuilder();
        sb.append("bootstrap.server.port=").append(port).append('\n');
        sb.append("bootstrap.server.host=").append(LOCALHOST).append('\n');
        sb.append("orientdb.connection.string=memory:").append(dbName);
        Files.write(sb.toString(), new File(tempDir, "gcplot.properties"), Charset.forName("UTF-8"));
    }

    public void intercept() {
    }

    protected void get(String path, long expectedError) throws Exception {
        get(path, a -> true, expectedError);
    }

    protected void get(String path, Predicate<JsonObject> test) throws Exception {
        get(path, test, -1);
    }

    protected void get(String path, Predicate<JsonObject> test, long expectedError) throws Exception {
        final CountDownLatch l = new CountDownLatch(1);
        if (path.startsWith("http")) {
            client.getAbs(path, r -> r.bodyHandler(b -> handleResponse(test, expectedError, l, b))).end();
        } else {
            client.get(port, LOCALHOST, path, r -> r.bodyHandler(b -> handleResponse(test, expectedError, l, b))).end();
        }
        Assert.assertTrue(l.await(3, TimeUnit.SECONDS));
    }

    protected void post(String path, String message, long expectedError) throws Exception {
        post(path, message, a -> true, expectedError);
    }

    protected void post(String path, Object message, long expectedError) throws Exception {
        post(path, JsonSerializer.serialize(message), a -> true, expectedError);
    }

    protected void post(String path, Object message, Predicate<JsonObject> test) throws Exception {
        post(path, JsonSerializer.serialize(message), test, -1);
    }

    protected void post(String path, String message, Predicate<JsonObject> test, long expectedError) throws Exception {
        final CountDownLatch l = new CountDownLatch(1);
        client.post(port, LOCALHOST, path, r -> r.bodyHandler(b -> handleResponse(test, expectedError, l, b))).end(message);
        Assert.assertTrue(l.await(300, TimeUnit.SECONDS));
    }

    private void handleResponse(Predicate<JsonObject> test, long expectedError, CountDownLatch l, Buffer b) {
        JsonObject jo = new JsonObject(new String(b.getBytes()));
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

    protected HttpClient client;
    protected static final String LOCALHOST = "127.0.0.1";
    protected static final Logger LOG = LoggerFactory.getLogger(IntegrationTest.class);
}
