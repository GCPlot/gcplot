package com.gcplot.repository;

import com.codahale.metrics.MetricRegistry;
import com.gcplot.Identifier;
import com.gcplot.commons.Metrics;
import com.gcplot.commons.exceptions.Exceptions;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePoolFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public abstract class AbstractOrientDbRepository {

    public void init() {
        try (OObjectDatabaseTx db = db()) {
            db.setAutomaticSchemaGeneration(true);
            OSchema schema = db.getMetadata().getSchema();
            init(db, schema);
        }
    }

    protected abstract void init(OObjectDatabaseTx db, OSchema schema);

    public void destroy() {
        try {
            if (!this.pool.isClosed()) {
                this.pool.close();
            }
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        } finally {
            isClosed = true;
        }
    }

    protected Object mapToEntityId(String id) {
        String[] parts = id.substring(1).split(Character.toString(ORID.SEPARATOR));
        if (!id.startsWith(Character.toString(ORID.PREFIX)) || parts.length != 2) {
            throw new IllegalArgumentException(String.format("Not OrientDb correct entity ID=[%s]", id));
        }
        return new ORecordId(Integer.parseInt(parts[0]), Long.parseLong(parts[1]));
    }

    protected OObjectDatabaseTx db() {
        try {
            OObjectDatabaseTx db = new OObjectDatabaseTx(pool.acquire());
            metrics().counter(POOL_ACQUIRE_METRIC).inc();
            return db;
        } catch (Exception e) {
            metrics().counter(POOL_EXHAUSTED_METRIC).inc();
            LOG.error(e.getMessage(), e);
            return new OObjectDatabaseTx(config.connectionString).open(config.user, config.password);
        }
    }

    protected ODatabaseDocumentTx docDb() {
        try {
            ODatabaseDocumentTx db = pool.acquire();
            metrics().counter(POOL_ACQUIRE_METRIC).inc();
            return db;
        } catch (Exception e) {
            metrics().counter(POOL_EXHAUSTED_METRIC).inc();
            LOG.error(e.getMessage(), e);
            return new ODatabaseDocumentTx(config.connectionString).open(config.user, config.password);
        }
    }

    protected <T> Optional<T> singleByQuery(String query) {
        try (OObjectDatabaseTx db = db()) {
            List<T> result = db.query(new OSQLSynchQuery<T>(query));
            if (result.size() > 1) {
                throw new IllegalStateException("Many users for the same id in database.");
            }
            if (result.size() > 0) {
                return Optional.of(db.detachAll(result.get(0), true));
            } else {
                return Optional.empty();
            }
        }
    }

    protected <T> Optional<T> get(Identifier id) {
        try (OObjectDatabaseTx db = db()) {
            Object oid = mapToEntityId(id.toString());
            if (oid != null) {
                return Optional.ofNullable(db.detachAll(db.load((ORID) oid), true));
            } else {
                return Optional.empty();
            }
        }
    }

    protected void executeTx(String cmd) {
        try (OObjectDatabaseTx db = db()) {
            db.begin();
            int i;
            try {
                i = db.command(new OCommandSQL(cmd)).execute();
            } catch (Throwable t) {
                db.rollback();
                throw Exceptions.runtime(t);
            }
            if (i != 1) {
                db.rollback();
                throw new IllegalArgumentException(String.format("Command [%s] didn't affected any record!", cmd));
            }
            db.commit();
        }
    }

    protected boolean execute(String cmd) {
        try {
            executeTx(cmd);
            return true;
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
            return false;
        }
    }

    public AbstractOrientDbRepository(OrientDbConfig config, OPartitionedDatabasePoolFactory poolFactory) {
        this.config = config;
        this.pool = poolFactory.get(config.connectionString, config.user, config.password);
    }

    protected MetricRegistry metrics;
    protected MetricRegistry metrics() {
        if (metrics != null) {
            return metrics;
        } else {
            metrics = new MetricRegistry();
            return metrics;
        }
    }
    public void setMetrics(MetricRegistry metrics) {
        this.metrics = metrics;
    }

    protected volatile boolean isClosed = false;
    protected OrientDbConfig config;
    protected OPartitionedDatabasePool pool;

    private static final String POOL_ACQUIRE_METRIC = Metrics.name(AccountOrientDbRepository.class, "pool");
    private static final String POOL_EXHAUSTED_METRIC = Metrics.name(AccountOrientDbRepository.class, "pool", "exhausted");
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractOrientDbRepository.class);
}
