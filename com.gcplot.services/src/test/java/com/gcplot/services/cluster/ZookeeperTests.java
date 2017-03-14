package com.gcplot.services.cluster;

import com.gcplot.cluster.Worker;
import com.gcplot.commons.FileUtils;
import com.gcplot.commons.Utils;
import com.gcplot.commons.exceptions.Exceptions;
import com.google.common.io.Files;
import org.apache.zookeeper.server.NIOServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/14/17
 */
public class ZookeeperTests {
    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperTests.class);
    private File zkDir;
    private Utils.Port zkPort;
    private NIOServerCnxnFactory standaloneServerFactory;

    @Before
    public void setUp() throws Exception {
        zkDir = Files.createTempDir().getAbsoluteFile();
        zkPort = Utils.getFreePorts(1)[0];
        ZooKeeperServer zkServer = new ZooKeeperServer(zkDir, zkDir, 2000);
        standaloneServerFactory = new NIOServerCnxnFactory();
        standaloneServerFactory.configure(new InetSocketAddress(zkPort.value), 5000);
        standaloneServerFactory.startup(zkServer);
    }

    @After
    public void tearDown() throws Exception {
        zkPort.unlock();
        try {
            standaloneServerFactory.shutdown();
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        } finally {
            FileUtils.deleteSilent(zkDir);
        }
    }

    @Test
    public void simpleOneNodeTest() throws Exception {
        ZookeeperConnector conn = createConnector();
        ZookeeperClusterManager zcm = createClusterManager(conn, "host1", "127.0.0.1");

        Assert.assertTrue(zcm.isMaster());
        Assert.assertTrue(zcm.isConnected());
        Assert.assertEquals(zcm.workers().size(), 1);
        Assert.assertEquals(zcm.retrieveTasks().size(), 0);
    }

    protected ZookeeperClusterManager createClusterManager(ZookeeperConnector conn, String hostname, String hostAddress) {
        ZookeeperClusterManager zcm = new ZookeeperClusterManager();
        zcm.setConnector(conn);
        zcm.setCurrentWorker(new Worker("test", hostname, hostAddress));
        zcm.setSyncElection(true);
        zcm.init();
        return zcm;
    }

    protected ZookeeperConnector createConnector() {
        ZookeeperConnector conn = new ZookeeperConnector();
        conn.setHost("localhost");
        conn.setPort(zkPort.value);
        conn.setSecret("123");
        conn.setUid("uid");
        conn.setSessionTimeout(5000);
        try {
            conn.init();
        } catch (Exception e) {
            throw Exceptions.runtime(e);
        }
        return conn;
    }

}
