package com.gcplot.cassandra;

import com.gcplot.commons.Utils;
import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.io.FileOutputStream;

public class BaseCassandra {
    protected static File storagedir;
    protected static File tempDir;
    protected static File commitLog;
    protected static File savedCaches;
    protected static File dataFiles;
    protected static int nativePort;
    protected static Utils.Port[] ports;

    @BeforeClass
    public static void before() throws Exception {
        storagedir = Files.createTempDir();
        tempDir = Files.createTempDir();
        commitLog = Files.createTempDir();
        savedCaches = Files.createTempDir();
        dataFiles = Files.createTempDir();
        File config = java.nio.file.Files.createTempFile("csnd", ".yaml").toFile();
        ports = Utils.getFreePorts(4);
        synchronized (CassandraConnector.class) {
            System.setProperty("cassandra.storagedir", storagedir.getAbsolutePath());
            String str = IOUtils.toString(BaseCassandra.class.getClassLoader().getResourceAsStream("cassandra.yaml"), "UTF-8");
            str = str.replace("{COMMIT_LOG}", commitLog.getAbsolutePath())
                    .replace("{SAVED_CACHES}", savedCaches.getAbsolutePath())
                    .replace("{DATA_FILES}", dataFiles.getAbsolutePath())
                    .replace("{STORAGE_PORT}", ports[0].value + "")
                    .replace("{STORAGE_SSL_PORT}", ports[1].value + "")
                    .replace("{NATIVE_PORT}", ports[2].value + "")
                    .replace("{RPC_PORT}", ports[3].value + "");
            nativePort = ports[2].value;
            try (FileOutputStream fos = new FileOutputStream(config)) {
                IOUtils.write(str, fos, "UTF-8");
            }
            EmbeddedCassandraServerHelper.startEmbeddedCassandra(config, tempDir.getAbsolutePath(), 60_000);
        }
    }

    @AfterClass
    public static void afterClass() {
        for (Utils.Port port : ports) {
            port.unlock();
        }
    }

    @After
    public void after() throws Exception {
        try {
            synchronized (CassandraConnector.class) {
                EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
            }
        } catch (Throwable ignored) {
        }
    }
}
