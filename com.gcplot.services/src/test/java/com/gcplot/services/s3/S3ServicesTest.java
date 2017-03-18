package com.gcplot.services.s3;

import com.gcplot.commons.FileUtils;
import com.gcplot.commons.Utils;
import com.gcplot.services.S3Connector;
import com.google.common.io.Files;
import io.findify.s3mock.S3Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

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
        }
    }

    @Test
    public void test() {
        S3Connector connector = new S3Connector();
        connector.setEndpoint(endpoint);
        connector.init();

        connector.getClient().createBucket("gcplot");

        
    }

}
