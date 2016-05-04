package com.gcplot.accounts.orientdb;

import com.gcplot.configuration.ConfigurationManager;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePoolFactory;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class OrientDbConfigurationManager extends AbstractOrientDbRepository implements ConfigurationManager {

    public OrientDbConfigurationManager(OrientDbConfig config, OPartitionedDatabasePoolFactory poolFactory) {
        super(config, poolFactory);
    }

    @Override
    protected void init(OObjectDatabaseTx db, OSchema schema) {
        if (!schema.existsClass(GCP_CONFIG)) {

        }
        // TODO load from db to configs
        executor.scheduleAtFixedRate(() -> {
            putLock.writeLock().lock();
            try {

            } finally {
                putLock.writeLock().unlock();
            }
        }, pullIntervalMs, pullIntervalMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void destroy() {
        super.destroy();

    }

    @Override
    public Object readProperty(String key) {
        return configs.get(key);
    }

    @Override
    public String readString(String key) {
        return (String) configs.get(key);
    }

    @Override
    public int readInt(String key) {
        return (int) configs.get(key);
    }

    @Override
    public long readLong(String key) {
        return (long) configs.get(key);
    }

    @Override
    public boolean readBoolean(String key) {
        return (boolean) configs.get(key);
    }

    @Override
    public double readDouble(String key) {
        return (double) configs.get(key);
    }

    @Override
    public void putProperty(String key, Object value) {
        putLock.readLock().lock();
        try {
            configs.put(key, value);

        } finally {
            putLock.readLock().unlock();
        }
    }

    protected long pullIntervalMs;
    public void setPullIntervalMs(long pullIntervalMs) {
        this.pullIntervalMs = pullIntervalMs;
    }

    protected ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    protected volatile Map<String, Object> configs = new ConcurrentHashMap<>();
    protected static final String GCP_CONFIG = "GCPConfig";
    protected static final ReadWriteLock putLock = new ReentrantReadWriteLock();
    protected static final Logger LOG = LoggerFactory.getLogger(OrientDbConfigurationManager.class);
}
