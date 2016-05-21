package com.gcplot.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.io.Files;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class TestCassandraConnector {
    protected File tempDir;

    @Before
    public void before() throws Exception {
        tempDir = Files.createTempDir();
        System.setProperty("cassandra.storagedir", tempDir.getAbsolutePath());
        EmbeddedCassandraServerHelper.startEmbeddedCassandra(EmbeddedCassandraServerHelper.CASSANDRA_RNDPORT_YML_FILE);
    }

    @After
    public void after() throws Exception {
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }

    @Test
    public void test() throws Exception {
        CassandraConnector connector = new CassandraConnector();
        connector.setHosts(new String[] { EmbeddedCassandraServerHelper.getHost() });
        connector.setPort(EmbeddedCassandraServerHelper.getNativeTransportPort());
        connector.init();

        ResultSet rs = connector.session().execute("select release_version from system.local");
        Row row = rs.one();
        row.getString("release_version");
        Assert.assertEquals(row.getString("release_version"), "3.4");

        connector.destroy();
    }

}
