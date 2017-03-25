package com.gcplot.cassandra;

import com.gcplot.commons.FileUtils;
import com.gcplot.commons.Utils;
import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/18/16
 */
public class CassandraServer {
    protected static File storagedir;
    protected static File tempDir;
    protected static File commitLog;
    protected static File savedCaches;
    protected static File dataFiles;
    protected static File configFile;
    protected static int nativePort;
    protected static Utils.Port[] ports;

    public synchronized void start() throws Exception {
        storagedir = Files.createTempDir();
        tempDir = Files.createTempDir();
        commitLog = Files.createTempDir();
        savedCaches = Files.createTempDir();
        dataFiles = Files.createTempDir();
        configFile = java.nio.file.Files.createTempFile("csnd", ".yaml").toFile();
        ports = Utils.getFreePorts(4);
        synchronized (CassandraConnector.class) {
            System.setProperty("cassandra.storagedir", storagedir.getAbsolutePath());
            String str = IOUtils.toString(CassandraServer.class.getClassLoader().getResourceAsStream("cassandra.yaml"), "UTF-8");
            str = str.replace("{COMMIT_LOG}", commitLog.getAbsolutePath())
                    .replace("{SAVED_CACHES}", savedCaches.getAbsolutePath())
                    .replace("{DATA_FILES}", dataFiles.getAbsolutePath())
                    .replace("{STORAGE_PORT}", ports[0].value + "")
                    .replace("{STORAGE_SSL_PORT}", ports[1].value + "")
                    .replace("{NATIVE_PORT}", ports[2].value + "")
                    .replace("{RPC_PORT}", ports[3].value + "");
            nativePort = ports[2].value;
            try (FileOutputStream fos = new FileOutputStream(configFile)) {
                IOUtils.write(str, fos, "UTF-8");
            }
            EmbeddedCassandraServerHelper.startEmbeddedCassandra(configFile, tempDir.getAbsolutePath(), 60_000);
        }
    }

    public synchronized void stop() {
        for (Utils.Port port : ports) {
            port.unlock();
        }
        FileUtils.deleteSilent(storagedir);
        FileUtils.deleteSilent(tempDir);
        FileUtils.deleteSilent(commitLog);
        FileUtils.deleteSilent(savedCaches);
        FileUtils.deleteSilent(dataFiles);
        configFile.delete();
    }

    public synchronized int getNativePort() {
        return nativePort;
    }

    public synchronized void clean() {
        try {
            synchronized (CassandraConnector.class) {
                EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
            }
        } catch (Throwable ignored) {
        }
    }

}
