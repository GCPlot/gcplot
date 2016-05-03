package com.gcplot.integration_tests;

import com.gcplot.bootstrap.Bootstrap;
import com.gcplot.commons.FileUtils;
import com.gcplot.commons.Utils;
import com.gcplot.web.Dispatcher;
import com.google.common.io.Files;
import org.junit.After;
import org.junit.Before;
import org.springframework.context.ApplicationContext;

import java.io.File;
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

    @Before
    public void setUp() throws Exception {
        tempDir = Files.createTempDir();
        port = Utils.getFreePorts(1)[0];
        Files.write("bootstrap.server.port=" + port, new File(tempDir, "gcplot.properties"), Charset.forName("UTF-8"));
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
        getBean(Dispatcher.class).close();
    }

    public void intercept() {
    }

}
