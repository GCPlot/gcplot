package com.gcplot.configuration;

import com.gcplot.repository.AbstractOrientDbRepository;
import com.gcplot.repository.OrientDbConfig;
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
                newClass.createProperty("hg", OType.STRING);
                newClass.createProperty("map", OType.EMBEDDEDMAP);
                newClass.createIndex("hg.unq", OClass.INDEX_TYPE.UNIQUE, "hg");
                db.commit(true);
                db.command(new OCommandSQL("INSERT INTO " + GCP_CONFIG +
                        " SET hg=\"" + hostGroup + "\", map={}")).execute();
            }
            fetchFromDb(db);
            executor.scheduleAtFixedRate(() -> {
                LOG.info("Trying to reload configuration from main DB.");
                putLock.writeLock().lock();
                try {
                    if (!isClosed) {
                        try (ODatabaseDocumentTx _db = docDb()) {
                            fetchFromDb(_db);
                        }
                    }
                } finally {
                    putLock.writeLock().unlock();
                }
            }, readLong(ConfigProperty.POLL_INTERVAL), readLong(ConfigProperty.POLL_INTERVAL), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    protected void init(OObjectDatabaseTx db, OSchema schema) {
    }

    @Override
    public void destroy() {
        putLock.writeLock().lock();
        try {
            super.destroy();
            executor.shutdownNow();
        } finally {
            putLock.writeLock().unlock();
        }
    }

    @Override
    public String readString(ConfigProperty configProperty) {
        return configs.getOrDefault(configProperty.getKey(), configProperty.getDefaultValue()).toString();
    }

    @Override
    public int readInt(ConfigProperty configProperty) {
        Number n = (Number) configs.get(configProperty.getKey());
        return n == null ? ((Number) configProperty.getDefaultValue()).intValue() : n.intValue();
    }

    @Override
    public long readLong(ConfigProperty configProperty) {
        Number n = (Number) configs.get(configProperty.getKey());
        return n == null ? ((Number) configProperty.getDefaultValue()).longValue() : n.longValue();
    }

    @Override
    public boolean readBoolean(ConfigProperty configProperty) {
        return (boolean) configs.getOrDefault(configProperty.getKey(), configProperty.getDefaultValue());
    }

    @Override
    public double readDouble(ConfigProperty configProperty) {
        Number n = (Number) configs.get(configProperty.getKey());
        return n == null ? ((Number) configProperty.getDefaultValue()).doubleValue() : n.doubleValue();
    }

    @Override
    public void putProperty(ConfigProperty key, Object value) {
        putLock.readLock().lock();
        try {
            configs.put(key.getKey(), value);
            String val = value.getClass().equals(String.class) ? "\"" + value + "\"" : value.toString();
            try (ODatabaseDocumentTx db = docDb()) {
                db.command(new OCommandSQL(String.format(UPDATE_COMMAND, key.getKey(), val, hostGroup))).execute();
            }
        } finally {
            putLock.readLock().unlock();
        }
    }

    protected void fetchFromDb(ODatabaseDocumentTx db) {
        List<ODocument> docs = db.query(new OSQLSynchQuery<>(String.format(GET_COMMAND, hostGroup)));
        if (docs.size() == 0 || docs.size() > 1) {
            LOG.error("Illegal OrientDB configuration state - " + docs.size() + " number of records.");
        }
        if (docs.size() > 0) {
            configs = new ConcurrentHashMap<>(docs.get(0).field("map"));
        }
    }

    protected String hostGroup;
    public String getHostGroup() {
        return hostGroup;
    }
    public void setHostGroup(String hostGroup) {
        this.hostGroup = hostGroup;
    }

    protected ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    protected volatile Map<String, Object> configs = new ConcurrentHashMap<>();
    protected final ReadWriteLock putLock = new ReentrantReadWriteLock();

    protected static final String GCP_CONFIG = "GCPConfig";
    protected static final String UPDATE_COMMAND = "UPDATE " + GCP_CONFIG + " PUT map = \"%s\", %s WHERE hg=\"%s\"";
    protected static final String GET_COMMAND = "SELECT FROM " + GCP_CONFIG + " WHERE hg=\"%s\"";
    protected static final Logger LOG = LoggerFactory.getLogger(OrientDbConfigurationManager.class);
}
