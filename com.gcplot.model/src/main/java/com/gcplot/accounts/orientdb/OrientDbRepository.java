package com.gcplot.accounts.orientdb;

import com.codahale.metrics.MetricRegistry;
import com.gcplot.Identifier;
import com.gcplot.accounts.Account;
import com.gcplot.accounts.AccountImpl;
import com.gcplot.accounts.AccountRepository;
import com.gcplot.commons.exceptions.Exceptions;
import com.gcplot.filters.FiltersRepository;
import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePoolFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
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
import java.util.stream.Collectors;

public class OrientDbRepository implements AccountRepository, FiltersRepository {

    public void init() {
        try (OObjectDatabaseTx db = db()) {
            db.setAutomaticSchemaGeneration(true);
            OSchema schema = db.getMetadata().getSchema();
            if (schema.getClass(ACCOUNT_DOCUMENT_NAME) == null) {
                db.getEntityManager().registerEntityClass(AccountImpl.class);
                OClass cls = db.getMetadata().getSchema().getClass(AccountImpl.class);
                String indexName = AccountImpl.class.getName() + ".unq";
                Table t = AccountImpl.class.getAnnotation(Table.class);
                if (t != null) {
                    Set<String> fields = new HashSet<>();
                    for (UniqueConstraint uc : t.uniqueConstraints()) {
                        fields.addAll(Lists.newArrayList(uc.columnNames()));
                    }
                    if (fields.size() > 0) {
                        LOG.info("Registering unique constraint for fields: " + fields);
                        cls.createIndex(indexName, OClass.INDEX_TYPE.UNIQUE_HASH_INDEX, (String[]) fields.toArray(new String[0]));
                    }
                }
            }

            if (schema.getClass("Filter") == null) {
                OClass filterClass = schema.createClass("Filter");
                filterClass.createProperty("type", OType.STRING);
                filterClass.createProperty("value", OType.STRING);
                filterClass.createIndex("filter.type.indices", OClass.INDEX_TYPE.FULLTEXT_HASH_INDEX, "type");
                filterClass.createIndex("filter.value.indices", OClass.INDEX_TYPE.FULLTEXT_HASH_INDEX, "value");
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
        metrics.counter(ALL_ACCOUNTS_METRIC).inc();
        try (OObjectDatabaseTx db = db()) {
            List<Account> l = db.query(new OSQLSynchQuery<>(ALL_ACCOUNTS_QUERY));
            l.forEach(i -> db.detachAll(i, true));
            return l;
        }
    }

    @Override
    public Optional<Account> account(Identifier id) {
        metrics.counter(ACCOUNT_METRIC).inc();
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
        metrics.counter(ACCOUNT_TOKEN_METRIC).inc();
        return singleByQuery(String.format(ACCOUNT_BY_TOKEN_QUERY, token));
    }

    @Override
    public Optional<Account> account(String username, String passHash, LoginType loginType) {
        metrics.counter(ACCOUNT_METRIC).inc();
        return singleByQuery(String.format(ACCOUNT_BY_USERNAME_QUERY, loginType == LoginType.USERNAME ? "username" : "email",
                username, passHash));
    }

    @Override
    public Account store(Account account) {
        metrics.counter(ACCOUNT_STORE_METRIC).inc();
        try (OObjectDatabaseTx db = db()) {
            return db.detachAll(db.save(account), true);
        }
    }

    @Override
    public void delete(Account account) {
        metrics.counter(ACCOUNT_DELETE_METRIC).inc();
        try (OObjectDatabaseTx db = db()) {
            db.delete((ORID) ((AccountImpl) account).getOId());
        }
    }

    @Override
    public boolean generateNewToken(String oldToken, String newToken) {
        metrics.counter(ACCOUNT_NEW_TOKEN_METRIC).inc();
        try (OObjectDatabaseTx db = db()) {
            db.begin();
            try {
                int i = db.command(new OCommandSQL(UPDATE_TOKEN_QUERY)).execute();
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
        metrics.counter(ACCOUNT_CONFIRM_METRIC).inc();
        try (OObjectDatabaseTx db = db()) {
            db.begin();
            try {
                int i = db.command(new OCommandSQL(String.format(CONFIRM_ACCOUNT_QUERY, token, salt))).execute();
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

    @Override
    public void block(String username) {
        block(username, true);
    }

    @Override
    public void unblock(String username) {
        block(username, false);
    }

    @Override
    public boolean isFiltered(String type, String value) {
        try (ODatabaseDocumentTx db = docDb()) {
            return db.query(new OSQLSynchQuery<>(String.format("select type from Filter where type=\"%s\"" +
                    " and value=\"%s\"", type, value))).size() > 0;
        }
    }

    @Override
    public void filter(String type, String value) {
        try (ODatabaseDocumentTx db = docDb()) {
            ODocument newFilter = db.newInstance("Filter");
            newFilter.field("type", type);
            newFilter.field("value", value);
            newFilter.save();
        }
    }

    @Override
    public void notFilter(String type, String value) {
        try (ODatabaseDocumentTx db = docDb()) {
            db.command(new OCommandSQL(String.format("delete from Filter where type=\"%s\" and value=\"%s\"",
                    type, value))).execute();
        }
    }

    @Override
    public List<String> getAllFiltered(String type) {
        try (ODatabaseDocumentTx db = docDb()) {
            List<ODocument> r = db.query(new OSQLSynchQuery<>(String.format("select value from Filter where type=\"%s\"", type)));
            return r.stream().map(o -> (String) o.rawField("value")).collect(Collectors.toList());
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

    protected void block(String username, boolean block) {
        try (OObjectDatabaseTx db = db()) {
            db.begin();
            int i;
            try {
                i = db.command(new OCommandSQL(String.format(SET_BLOCKED_COMMAND, block, username))).execute();
            } catch (Throwable t) {
                db.rollback();
                throw Exceptions.runtime(t);
            }
            if (i != 1) {
                db.rollback();
                throw new IllegalArgumentException(String.format("User with [username=%s] can't be blocked!", username));
            }
            db.commit();
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
    private static final String ACCOUNT_DOCUMENT_NAME = AccountImpl.class.getSimpleName();
    private static final String ALL_ACCOUNTS_QUERY = "select from " + ACCOUNT_DOCUMENT_NAME;
    private static final String ACCOUNT_BY_TOKEN_QUERY = "select from " + ACCOUNT_DOCUMENT_NAME + " where token = \"%s\"";
    private static final String ACCOUNT_BY_USERNAME_QUERY = "select from " + ACCOUNT_DOCUMENT_NAME +
            " where %s = \"%s\" and passHash = \"%s\"";
    private static final String UPDATE_TOKEN_QUERY = "update " + ACCOUNT_DOCUMENT_NAME +
            " set token=\"%s\" where token=\"%s\"";
    private static final String CONFIRM_ACCOUNT_QUERY = "update " + ACCOUNT_DOCUMENT_NAME +
            " set confirmed=true where token=\"%s\" and confirmationSalt=\"%s\"";
    private static final String SET_BLOCKED_COMMAND = "update " + ACCOUNT_DOCUMENT_NAME +
            " set blocked=%s where username=\"%s\"";

    private static final String POOL_ACQUIRE_METRIC = MetricRegistry.name(OrientDbRepository.class, "pool");
    private static final String POOL_EXHAUSTED_METRIC = MetricRegistry.name(OrientDbRepository.class, "pool", "exhausted");
    private static final String ALL_ACCOUNTS_METRIC = MetricRegistry.name(OrientDbRepository.class, "all_accounts");
    private static final String ACCOUNT_METRIC = MetricRegistry.name(OrientDbRepository.class, "account");
    private static final String ACCOUNT_TOKEN_METRIC = MetricRegistry.name(OrientDbRepository.class, "account", "token");
    private static final String ACCOUNT_STORE_METRIC = MetricRegistry.name(OrientDbRepository.class, "account", "store");
    private static final String ACCOUNT_DELETE_METRIC = MetricRegistry.name(OrientDbRepository.class, "account", "delete");
    private static final String ACCOUNT_NEW_TOKEN_METRIC = MetricRegistry.name(OrientDbRepository.class, "account", "new_token");
    private static final String ACCOUNT_CONFIRM_METRIC = MetricRegistry.name(OrientDbRepository.class, "account", "confirm");
}
