package com.gcplot.cassandra;

import com.codahale.metrics.MetricRegistry;
import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class CassandraConnector {

    public void init() {
        LOG.info("Starting Cassandra connector initialization.");
        Cluster.Builder builder = Cluster.builder()
                .addContactPoints(hosts)
                .withReconnectionPolicy(new ConstantReconnectionPolicy(reconnectionDelayMs))
                .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
                .withCompression(ProtocolOptions.Compression.LZ4)
                .withSocketOptions(new SocketOptions()
                        .setReceiveBufferSize(receiverBufferSize)
                        .setSendBufferSize(senderBufferSize))
                .withPort(port);
        if (poolingOptions != null) {
            int procs = Runtime.getRuntime().availableProcessors();
            poolingOptions
                    .setConnectionsPerHost(HostDistance.LOCAL, procs, procs * 2)
                    .setConnectionsPerHost(HostDistance.REMOTE, (procs / 2), procs * 2)
                    .setPoolTimeoutMillis(poolTimeoutMillis)
                    .setMaxRequestsPerConnection(HostDistance.LOCAL, maxRequestsPerConnection)
                    .setMaxRequestsPerConnection(HostDistance.REMOTE, maxRequestsPerConnection);
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

    protected int receiverBufferSize = 2 * 1024 * 1024;
    public void setReceiverBufferSize(int receiverBufferSize) {
        this.receiverBufferSize = receiverBufferSize;
    }

    protected int senderBufferSize = 2 * 1024 * 1024;
    public void setSenderBufferSize(int senderBufferSize) {
        this.senderBufferSize = senderBufferSize;
    }

    protected int poolTimeoutMillis = (int) TimeUnit.SECONDS.toMillis(60);
    public void setPoolTimeoutMillis(int poolTimeoutMillis) {
        this.poolTimeoutMillis = poolTimeoutMillis;
    }

    protected int maxRequestsPerConnection = 1024;
    public void setMaxRequestsPerConnection(int maxRequestsPerConnection) {
        this.maxRequestsPerConnection = maxRequestsPerConnection;
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
