package com.gcplot.cassandra;

import com.codahale.metrics.MetricRegistry;
import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraConnector {

    public void init() {
        LOG.info("Starting Cassandra connector initialization.");
        Cluster.Builder builder = Cluster.builder()
                .addContactPoints(hosts)
                .withRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
                .withReconnectionPolicy(new ConstantReconnectionPolicy(reconnectionDelayMs))
                .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
                .withCompression(ProtocolOptions.Compression.LZ4)
                .withPort(port);
        if (poolingOptions != null) {
            int procs = Runtime.getRuntime().availableProcessors();
            poolingOptions
                    .setConnectionsPerHost(HostDistance.LOCAL, procs, procs * 2)
                    .setConnectionsPerHost(HostDistance.REMOTE, (procs / 2), procs * 2)
                    .setMaxRequestsPerConnection(HostDistance.LOCAL, 32768)
                    .setMaxRequestsPerConnection(HostDistance.REMOTE, 2000);
            builder.withPoolingOptions(poolingOptions);
        }
        if (!Strings.isNullOrEmpty(username)) {
            builder.withCredentials(username, password);
        }
        cluster = builder.build();
        session = cluster.connect(keyspace);
    }

    public void destroy() {
        LOG.info("Destroying Cassandra connector.");
        try {
            cluster.close();
        } catch (Throwable t) {
            LOG.error("CASSANDRA", t.getMessage());
        }
    }

    protected String[] hosts = LOCALHOST;
    public void setHosts(String[] hosts) {
        this.hosts = hosts;
    }

    protected int port = 9042;
    public void setPort(int port) {
        this.port = port;
    }

    protected String keyspace = "gcplot";
    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    protected String username;
    public void setUsername(String username) {
        this.username = username;
    }

    protected String password;
    public void setPassword(String password) {
        this.password = password;
    }

    protected long reconnectionDelayMs = 100L;
    public void setReconnectionDelayMs(long reconnectionDelayMs) {
        this.reconnectionDelayMs = reconnectionDelayMs;
    }

    protected MetricRegistry metrics;
    public void setMetrics(MetricRegistry metrics) {
        this.metrics = metrics;
    }
    public MetricRegistry getMetrics() {
        return metrics;
    }

    protected PoolingOptions poolingOptions;
    public void setPoolingOptions(PoolingOptions poolingOptions) {
        this.poolingOptions = poolingOptions;
    }

    protected Cluster cluster;
    public Cluster cluster() {
        return cluster;
    }

    protected Session session;
    public Session session() {
        return session;
    }

    protected static final String[] LOCALHOST = { "127.0.0.1" };
    protected static final Logger LOG = LoggerFactory.getLogger(CassandraConnector.class);
}
