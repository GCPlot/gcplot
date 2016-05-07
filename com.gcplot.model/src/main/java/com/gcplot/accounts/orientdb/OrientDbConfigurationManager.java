package com.gcplot.accounts.orientdb;

import com.gcplot.commons.Configuration;
import com.gcplot.configuration.ConfigurationManager;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePoolFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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
    public void init() {
        try (ODatabaseDocumentTx db = docDb()) {
            OSchema schema = db.getMetadata().getSchema();
            if (!schema.existsClass(GCP_CONFIG)) {
                OClass newClass = schema.createClass(GCP_CONFIG);
                newClass.createProperty("id", OType.INTEGER);
                newClass.createProperty("map", OType.EMBEDDEDMAP);
                newClass.createIndex("id.unq", OClass.INDEX_TYPE.UNIQUE, "id");
                db.commit(true);
                db.command(new OCommandSQL("INSERT INTO " + GCP_CONFIG +
                        " SET id = " + DEFAULT_ID + ", map={}")).execute();
            }
            fetchFromDb(db);
            if (pullIntervalMs > 0) {
                executor.scheduleAtFixedRate(() -> {
                    putLock.writeLock().lock();
                    try {
                        try (ODatabaseDocumentTx _db = docDb()) {
                            fetchFromDb(_db);
                        }
                    } finally {
                        putLock.writeLock().unlock();
                    }
                }, pullIntervalMs, pullIntervalMs, TimeUnit.MILLISECONDS);
            }
        }
    }

    @Override
    protected void init(OObjectDatabaseTx db, OSchema schema) {
    }

    @Override
    public void destroy() {
        super.destroy();
        executor.shutdownNow();
    }

    @Override
    public String readString(Configuration configuration) {
        return (String) configs.getOrDefault(configuration.getKey(), configuration.getDefaultValue());
    }

    @Override
    public int readInt(Configuration configuration) {
        Number n = (Number) configs.get(configuration.getKey());
        return n == null ? (int) configuration.getDefaultValue() : n.intValue();
    }

    @Override
    public long readLong(Configuration configuration) {
        Number n = (Number) configs.get(configuration.getKey());
        return n == null ? (long) configuration.getDefaultValue() : n.longValue();
    }

    @Override
    public boolean readBoolean(Configuration configuration) {
        return (boolean) configs.getOrDefault(configuration.getKey(), configuration.getDefaultValue());
    }

    @Override
    public double readDouble(Configuration configuration) {
        Number n = (Number) configs.get(configuration.getKey());
        return n == null ? (double) configuration.getDefaultValue() : n.doubleValue();
    }

    @Override
    public void putProperty(Configuration key, Object value) {
        putLock.readLock().lock();
        try {
            configs.put(key.getKey(), value);
            String val = value.getClass().equals(String.class) ? "\"" + value + "\"" : value.toString();
            try (ODatabaseDocumentTx db = docDb()) {
                db.command(new OCommandSQL(String.format(UPDATE_COMMAND, key.getKey(), val))).execute();
            }
        } finally {
            putLock.readLock().unlock();
        }
    }

    protected void fetchFromDb(ODatabaseDocumentTx db) {
        List<ODocument> docs = db.query(new OSQLSynchQuery<>(GET_COMMAND));
        if (docs.size() == 0 || docs.size() > 1) {
            LOG.error("Illegal OrientDB configuration state - " + docs.size() + " number of records.");
        }
        if (docs.size() > 0) {
            configs = new ConcurrentHashMap<>(docs.get(0).field("map"));
        }
    }

    protected long pullIntervalMs;
    public void setPullIntervalMs(long pullIntervalMs) {
        this.pullIntervalMs = pullIntervalMs;
    }

    protected ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    protected volatile Map<String, Object> configs = new ConcurrentHashMap<>();
    protected final ReadWriteLock putLock = new ReentrantReadWriteLock();

    protected static final int DEFAULT_ID = 1;
    protected static final String GCP_CONFIG = "GCPConfig";
    protected static final String UPDATE_COMMAND = "UPDATE " + GCP_CONFIG + " PUT map = \"%s\", %s WHERE id=" + DEFAULT_ID;
    protected static final String GET_COMMAND = "SELECT FROM " + GCP_CONFIG + " WHERE id=" + DEFAULT_ID;
    protected static final Logger LOG = LoggerFactory.getLogger(OrientDbConfigurationManager.class);
}
