package com.gcplot.repository;

import com.gcplot.cassandra.CassandraConnector;
import com.google.common.base.Preconditions;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/4/16
 */
public abstract class AbstractCassandraRepository {

    public void init() {
        Preconditions.checkNotNull(connector, "Cassandra connector is required.");
    }

    protected CassandraConnector connector;
    public CassandraConnector getConnector() {
        return connector;
    }
    public void setConnector(CassandraConnector connector) {
        this.connector = connector;
    }

    protected int fetchSize = 2000;
    public int getFetchSize() {
        return fetchSize;
    }
    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

}
