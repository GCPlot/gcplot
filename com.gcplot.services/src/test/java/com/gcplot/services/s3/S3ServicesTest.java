package com.gcplot.services.s3;

import com.gcplot.commons.FileUtils;
import com.gcplot.commons.Utils;
import com.gcplot.fs.LogsStorage;
import com.gcplot.logs.LogHandle;
import com.gcplot.logs.LogSource;
import com.gcplot.model.gc.SourceType;
import com.gcplot.services.S3Connector;
import com.gcplot.services.logs.DefaultLogsStorageProvider;
import com.gcplot.services.resources.S3ResourceManager;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import io.findify.s3mock.S3Mock;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/18/17
 */
public class S3ServicesTest {
    private Utils.Port port;
    private String endpoint;
    private File tempDir;
    private S3Mock s3Mock;

    @Before
    public void setUp() throws Exception {
        port = Utils.getFreePorts(1)[0];
        tempDir = Files.createTempDir().getAbsoluteFile();
        s3Mock = S3Mock.create(port.value, tempDir.getAbsolutePath());
        s3Mock.start();
        endpoint = "http://127.0.0.1:" + port.value;
    }

    @After
    public void tearDown() throws Exception {
        try {
            s3Mock.stop();
        } finally {
            FileUtils.deleteSilent(tempDir);
            port.unlock();
        }
    }

    @Test
    public void test() throws URISyntaxException, IOException {
        S3Connector connector = getConnector();
        S3ResourceManager rm = new S3ResourceManager();
        rm.setConnector(connector);

        rm.upload(file("logs/3cbc9e4b0932ca6e745b3f344a27ff95.log"),
                "connector-logs/" + Utils.toBase64("#81:20") + "/analyze/jvm");

        DefaultLogsStorageProvider logsStorageProvider = new DefaultLogsStorageProvider();
        logsStorageProvider.setPrefix("connector-logs");
        logsStorageProvider.setInternalConnector(connector);
        logsStorageProvider.init();

        LogsStorage ls = logsStorageProvider.get(SourceType.INTERNAL, null);
        List<LogHandle> lhl = Lists.newArrayList(ls.listAll());
        Assert.assertEquals(lhl.size(), 1);
        LogHandle lh = lhl.get(0);

        Assert.assertEquals(lh.getName(), "3cbc9e4b0932ca6e745b3f344a27ff95.log");
        Assert.assertEquals(lh.getAccountId(), "#81:20");
        Assert.assertEquals(lh.getAnalyzeId(), "analyze");
        Assert.assertEquals(lh.getJvmId(), "jvm");

        LogSource source = ls.get(lh);
        Assert.assertEquals(source.localFile(), Optional.empty());
        Assert.assertEquals(source.checksum(), "3cbc9e4b0932ca6e745b3f344a27ff95");
        Assert.assertEquals(source.isGzipped(), false);
        Assert.assertArrayEquals(IOUtils.toByteArray(source.logStream()),
                IOUtils.toByteArray(new FileInputStream(file("logs/3cbc9e4b0932ca6e745b3f344a27ff95.log"))));

        ls.delete(lh);
        lhl = Lists.newArrayList(ls.listAll());
        Assert.assertEquals(lhl.size(), 0);

        rm.upload(file("logs/3cbc9e4b0932ca6e745b3f344a27ff95.log.gz"),
                "connector-logs/" + Utils.toBase64("#81:20") + "/analyze/jvm");
        lhl = Lists.newArrayList(ls.listAll());
        Assert.assertEquals(lhl.size(), 1);
        lh = lhl.get(0);
        source = ls.get(lh);
        Assert.assertEquals(source.checksum(), "3cbc9e4b0932ca6e745b3f344a27ff95");
        Assert.assertEquals(source.isGzipped(), true);
        Assert.assertArrayEquals(IOUtils.toByteArray(source.logStream()),
                IOUtils.toByteArray(new FileInputStream(file("logs/3cbc9e4b0932ca6e745b3f344a27ff95.log"))));
    }

    protected S3Connector getConnector() {
        S3Connector connector = new S3Connector();
        connector.setEndpoint(endpoint);
        connector.setBucket("gcplot");
        connector.init();

        connector.getClient().createBucket("gcplot");
        return connector;
    }

    protected File file(String file) throws URISyntaxException {
        return new File(Thread.currentThread().
                getContextClassLoader().getResource(file).toURI());
    }

}
