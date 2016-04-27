package com.gcplot.model.orientdb;

import com.codahale.metrics.MetricRegistry;
import com.gcplot.Identifier;
import com.gcplot.model.Account;
import com.gcplot.model.AccountImpl;
import com.gcplot.model.AccountRepository;
import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePoolFactory;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class OrientDbRepository implements AccountRepository {

    public void init() {
        try (OObjectDatabaseTx db = db()) {
            db.setAutomaticSchemaGeneration(true);
            if (!db.getEntityManager().getRegisteredEntities().contains(AccountImpl.class)) {
                db.getEntityManager().registerEntityClass(AccountImpl.class);
            }
            OClass cls = db.getMetadata().getSchema().getClass(AccountImpl.class);
            String indexName = AccountImpl.class.getName() + ".unq";
            if (cls.getClassIndex(indexName) == null) {
                Table t = AccountImpl.class.getAnnotation(Table.class);
                if (t != null) {
                    Set<String> fields = new HashSet<>();
                    for (UniqueConstraint uc : t.uniqueConstraints()) {
                        fields.addAll(Lists.newArrayList(uc.columnNames()));
                    }
                    if (fields.size() > 0) {
                        LOG.info("Registering unqiue constraint for fields: " + fields);
                        cls.createIndex(indexName, OClass.INDEX_TYPE.UNIQUE, (String[]) fields.toArray(new String[0]));
                    }
                }
            }
        }
    }

    public void destroy() {
        try {
            this.pool.close();
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
    }

    @Override
    public List<Account> accounts() {
        try (OObjectDatabaseTx db = db()) {
            List<Account> l = db.query(new OSQLSynchQuery<>("select from AccountImpl"));
            l.forEach(i -> db.detachAll(i, true));
            return l;
        }
    }

    @Override
    public Optional<Account> account(Identifier id) {
        try (OObjectDatabaseTx db = db()) {
            Object oid = mapToEntityId(id.toString());
            if (oid != null) {
                return Optional.ofNullable(db.detachAll(db.load((ORID) oid), true));
            } else {
                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<Account> account(String token) {
        return singleByQuery(String.format("select from AccountImpl where token = \"%s\"", token));
    }

    @Override
    public Optional<Account> account(String username, String passHash) {
        return singleByQuery(String.format("select from AccountImpl where username = \"%s\" and passHash = \"%s\"",
                username, passHash));
    }

    @Override
    public Account store(Account account) {
        try (OObjectDatabaseTx db = db()) {
            return db.detachAll(db.save(account), true);
        }
    }

    @Override
    public void delete(Account account) {
        try (OObjectDatabaseTx db = db()) {
            db.delete((ORID) ((AccountImpl) account).getOId());
        }
    }

    @Override
    public boolean generateNewToken(String oldToken, String newToken) {
        try (OObjectDatabaseTx db = db()) {
            db.begin();
            try {
                int i = db.command(new OCommandSQL("update AccountImpl set token=\"%s\" where token=\"%s\"")).execute();
                if (i != 1) {
                    return false;
                }
            } catch (Throwable t) {
                db.rollback();
                LOG.error(t.getMessage(), t);
                return false;
            }
            db.commit();
            return true;
        }
    }

    @Override
    public boolean confirm(String token, String salt) {
        try (OObjectDatabaseTx db = db()) {
            db.begin();
            try {
                int i = db.command(new OCommandSQL(String.format("update AccountImpl set confirmed=true where token=\"%s\" and confirmationSalt=\"%s\"",
                        token, salt))).execute();
                if (i != 1) {
                    db.rollback();
                    return false;
                }
            } catch (Throwable t) {
                db.rollback();
                LOG.error(t.getMessage(), t);
                return false;
            }
            db.commit();
            return true;
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
            metrics().counter(MetricRegistry.name(OrientDbRepository.class, "pool")).inc();
            return db;
        } catch (Exception e) {
            metrics().counter(MetricRegistry.name(OrientDbRepository.class, "pool", "exhausted")).inc();
            LOG.error(e.getMessage(), e);
            return new OObjectDatabaseTx(config.connectionString).open(config.user, config.password);
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

    protected MetricRegistry metrics() {
        if (metrics != null) {
            return metrics;
        } else {
            metrics = new MetricRegistry();
            return metrics;
        }
    }

    public OrientDbRepository(OrientDbConfig config, OPartitionedDatabasePoolFactory poolFactory) {
        this.config = config;
        this.pool = poolFactory.get(config.connectionString, config.user, config.password);
    }

    protected MetricRegistry metrics;
    public void setMetrics(MetricRegistry metrics) {
        this.metrics = metrics;
    }

    protected OrientDbConfig config;
    protected OPartitionedDatabasePool pool;

    protected static final Logger LOG = LoggerFactory.getLogger(OrientDbRepository.class);
}
