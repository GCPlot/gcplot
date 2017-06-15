package com.gcplot.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Assert;
import org.junit.Test;

public class TestCassandraConnector extends BaseCassandra {

    @Test
    public void test() throws Exception {
        CassandraConnector connector = new CassandraConnector();
        connector.setHosts(new String[] { EmbeddedCassandraServerHelper.getHost() });
        connector.setPort(server.getNativePort());
        connector.setKeyspace(null);
        connector.init();

        ResultSet rs = connector.session().execute("select release_version from system.local");
        Row row = rs.one();
        row.getString("release_version");
        Assert.assertEquals(row.getString("release_version"), "3.9");

        connector.session().execute("CREATE KEYSPACE gcplot WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }");

        connector.destroy();

        connector = new CassandraConnector();
        connector.setHosts(new String[] { EmbeddedCassandraServerHelper.getHost() });
        connector.setPort(server.getNativePort());
        connector.setKeyspace("gcplot");
        connector.init();
        connector.destroy();
    }

}
