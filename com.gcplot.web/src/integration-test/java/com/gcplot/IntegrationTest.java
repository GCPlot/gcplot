package com.gcplot;

import com.gcplot.bootstrap.Bootstrap;
import com.gcplot.commons.FileUtils;
import com.gcplot.commons.Utils;
import com.gcplot.web.Dispatcher;
import com.google.common.io.Files;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public abstract class IntegrationTest {

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
        dbName = Utils.getRandomIdentifier();
        database = new ODatabaseDocumentTx("memory:" + dbName).create();

        makeTestPropertiesFile();
        bootstrap = new Bootstrap();
        bootstrap.hang = false;
        bootstrap.configDir = tempDir.getAbsolutePath();
        intercept();
        bootstrap.run();
    }

    @After
    public void tearDown() throws Exception {
        try {
            FileUtils.delete(tempDir);
        } catch (Throwable ignored) {
        }
        try {
            new ODatabaseDocumentTx("memory:" + dbName).open("admin", "admin");
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
        getBean(Dispatcher.class).close();
    }

    protected void makeTestPropertiesFile() throws IOException {
        tempDir = Files.createTempDir();
        port = Utils.getFreePorts(1)[0];
        StringBuilder sb = new StringBuilder();
        sb.append("bootstrap.server.port=" + port).append('\n');
        sb.append("orientdb.connection.string=memory:" + dbName);
        Files.write(sb.toString(), new File(tempDir, "gcplot.properties"), Charset.forName("UTF-8"));
    }

    public void intercept() {
    }

    protected static final Logger LOG = LoggerFactory.getLogger(IntegrationTest.class);
}
