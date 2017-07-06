package com.gcplot.repository;

import com.gcplot.Identifier;
import com.gcplot.commons.Metrics;
import com.gcplot.model.account.config.ConfigProperty;
import com.gcplot.utils.Exceptions;
import com.gcplot.commons.exceptions.NotUniqueException;
import com.gcplot.model.account.Account;
import com.gcplot.model.account.AccountImpl;
import com.gcplot.model.role.Role;
import com.gcplot.model.role.RoleImpl;
import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePoolFactory;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;
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

public class AccountOrientDbRepository extends AbstractOrientDbRepository implements AccountRepository {

    public AccountOrientDbRepository(OrientDbConfig config, OPartitionedDatabasePoolFactory poolFactory) {
        super(config, poolFactory);
    }

    @Override
    public void init(OObjectDatabaseTx db, OSchema schema) {
        db.getEntityManager().registerEntityClass(RoleImpl.RestrictionImpl.class);
        db.getEntityManager().registerEntityClass(RoleImpl.class);
        if (schema.getClass(ACCOUNT_DOCUMENT_NAME) == null) {
            db.getEntityManager().registerEntityClasses(AccountImpl.class, true);
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
                    for (String field : fields)
                        cls.createIndex(indexName + "." + field, OClass.INDEX_TYPE.UNIQUE_HASH_INDEX, field);
                }
            }
        } else {
            db.getEntityManager().registerEntityClasses(AccountImpl.class, true);
        }
    }

    @Override
    public List<Account> accounts() {
        metrics.meter(ALL_ACCOUNTS_METRIC).mark();
        try (OObjectDatabaseTx db = db()) {
            List<Account> l = db.query(new OSQLSynchQuery<>(ALL_ACCOUNTS_QUERY));
            return l.stream().map(i -> (AccountImpl) db.detachAll(i, true)).collect(Collectors.toList());
        }
    }

    @Override
    public Optional<Identifier> map(String username) {
        try (OObjectDatabaseTx db = db()) {
            List<ODocument> o = db.query(new OSQLSynchQuery<>(String.format(MAP_ACCOUNT_QUERY, username)));
            if (o.size() == 0) {
                return Optional.empty();
            } else {
                return Optional.of(Identifier.fromStr(o.get(0).field("rid").toString()));
            }
        }
    }

    @Override
    public Optional<Account> account(Identifier id) {
        metrics.meter(ACCOUNT_METRIC).mark();
        return get(id);
    }

    @Override
    public Optional<Account> account(String token) {
        metrics.meter(ACCOUNT_TOKEN_METRIC).mark();
        return singleByQuery(String.format(ACCOUNT_BY_TOKEN_QUERY, token));
    }

    @Override
    public Optional<Account> account(String username, String passHash, LoginType loginType) {
        metrics.meter(ACCOUNT_METRIC).mark();
        return singleByQuery(String.format(ACCOUNT_BY_USERNAME_QUERY, loginType == LoginType.USERNAME ? "username" : "email",
                username, passHash));
    }

    @Override
    public Optional<Account> find(String username, LoginType type) {
        return singleByQuery(String.format(FIND_ACCOUNT_BY_USERNAME, type == LoginType.USERNAME ? "username" : "email",
                username));
    }

    @Override
    public Account insert(Account account) {
        metrics.meter(ACCOUNT_INSERT_METRIC).mark();
        try (OObjectDatabaseTx db = db()) {
            try {
                return db.detachAll(db.save(account), true);
            } catch (ORecordDuplicatedException e) {
                throw new NotUniqueException(e.getMessage());
            }
        }
    }

    @Override
    public Account updateInfo(Account account) {
        metrics.meter(ACCOUNT_UPDATE_INFO_METRIC).mark();
        try (OObjectDatabaseTx db = db()) {
            int i;
            try {
                i = db.command(new OCommandSQL(String.format(UPDATE_INFO_COMMAND, account.id().toString(),
                        account.username(), account.email(), account.firstName(), account.lastName(),
                        account.passHash()))).execute();
            } catch (Throwable t) {
                throw Exceptions.runtime(t);
            }
            if (i != 1) {
                throw new IllegalArgumentException(String.format("Can't update account %s with data %s", account.id(), account));
            }
            return account;
        }
    }

    @Override
    public void delete(Account account) {
        metrics.meter(ACCOUNT_DELETE_METRIC).mark();
        try (OObjectDatabaseTx db = db()) {
            db.delete((ORID) ((AccountImpl) account).getOId());
        }
    }

    @Override
    public boolean generateNewToken(String oldToken, String newToken) {
        metrics.meter(ACCOUNT_NEW_TOKEN_METRIC).mark();
        return execute(String.format(UPDATE_TOKEN_QUERY, newToken, oldToken));
    }

    @Override
    public boolean changePassword(Account account, String newPasshash) {
        metrics.meter(ACCOUNT_NEW_PASSWORD_METRIC).mark();
        return execute(String.format(UPDATE_PASSWORD_COMMAND, account.id().toString(), newPasshash));
    }

    @Override
    public boolean changeUsername(Account account, String newUsername) {
        metrics.meter(ACCOUNT_NEW_USERNAME_METRIC).mark();
        return execute(String.format(UPDATE_USERNAME_COMMAND, account.id().toString(), newUsername));
    }

    @Override
    public boolean changeEmail(Account account, String newEmail) {
        metrics.meter(ACCOUNT_NEW_EMAIL_METRIC).mark();
        return execute(String.format(UPDATE_EMAIL_COMMAND, account.id().toString(), newEmail));
    }

    @Override
    public boolean confirm(String token, String salt) {
        metrics.meter(ACCOUNT_CONFIRM_METRIC).mark();
        return execute(String.format(CONFIRM_ACCOUNT_QUERY, token, salt));
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
    public void attachNewIp(Account account, String ip) {
        if (account.ips().size() == 0) {
            execute(String.format(UPDATE_IP_FIELD_COMMAND, account.id().toString()));
        }
        execute(String.format(ATTACH_IP_COMMAND, account.id().toString(), ip));
    }

    @Override
    public void attachRole(Account account, Role role) {
        ((AccountImpl)account).addRole((RoleImpl) role);
        updateRoles(((AccountImpl)account).rolesImpl(), account);
    }

    @Override
    public void detachRole(Account account, Role role) {
        ((AccountImpl)account).removeRole((RoleImpl) role);
        updateRoles(((AccountImpl)account).rolesImpl(), account);
    }

    @Override
    public void roleManagement(Identifier id, boolean isRoleManagement) {
        execute(String.format(UPDATE_ROLE_MANAGEMENT_COMMAND, id.toString(), isRoleManagement));
    }

    @Override
    public void updateConfig(Account account, ConfigProperty cp, String val) {
        metrics.meter(ACCOUNT_UPDATE_CONFIG_METRIC).mark();
        ((AccountImpl)account).getConfigs().put(cp.getId(), val);
        execute(String.format(UPDATE_CONFIG_COMMAND, account.id().toString(), cp.getId(), val));
    }

    protected void updateRoles(List<RoleImpl> roles, Account account) {
        metrics.meter(ACCOUNT_UPDATE_ROLES_METRIC).mark();
        try (OObjectDatabaseTx db = db()) {
            int i;
            try {
                i = db.command(new OCommandSQL(String.format(UPDATE_ROLES_COMMAND, account.id().toString(),
                        roles.stream().map(r -> r.id().toString()).collect(Collectors.joining(","))))).execute();
            } catch (Throwable t) {
                throw Exceptions.runtime(t);
            }
            if (i != 1) {
                throw new IllegalArgumentException(String.format("Can't udate account %s with roles %s", account.id(), roles));
            }
        }
    }

    protected void block(String username, boolean block) {
        executeTx(String.format(SET_BLOCKED_COMMAND, block, username));
    }

    protected static final Logger LOG = LoggerFactory.getLogger(AccountOrientDbRepository.class);
    private static final String ACCOUNT_DOCUMENT_NAME = AccountImpl.class.getSimpleName();
    private static final String ALL_ACCOUNTS_QUERY = "select from " + ACCOUNT_DOCUMENT_NAME;
    private static final String MAP_ACCOUNT_QUERY = "select @rid.asString() from " + ACCOUNT_DOCUMENT_NAME + " where username=\"%s\"";
    private static final String ACCOUNT_BY_TOKEN_QUERY = "select from " + ACCOUNT_DOCUMENT_NAME + " where token = \"%s\"";
    private static final String FIND_ACCOUNT_BY_USERNAME = "select from " + ACCOUNT_DOCUMENT_NAME +
            " where %s = \"%s\"";
    private static final String ACCOUNT_BY_USERNAME_QUERY = "select from " + ACCOUNT_DOCUMENT_NAME +
            " where %s = \"%s\" and passHash = \"%s\"";
    private static final String UPDATE_TOKEN_QUERY = "update " + ACCOUNT_DOCUMENT_NAME +
            " set token=\"%s\" where token=\"%s\" LOCK RECORD";
    private static final String CONFIRM_ACCOUNT_QUERY = "update " + ACCOUNT_DOCUMENT_NAME +
            " set confirmed=true where token=\"%s\" and confirmationSalt=\"%s\" LOCK RECORD";
    private static final String SET_BLOCKED_COMMAND = "update " + ACCOUNT_DOCUMENT_NAME +
            " set blocked=%s where username=\"%s\" LOCK RECORD";
    private static final String UPDATE_ROLES_COMMAND = "update %s" +
            " set roles=[%s] LOCK RECORD";
    private static final String UPDATE_INFO_COMMAND = "update %s" +
            " set username=\"%s\", email=\"%s\", firstName=\"%s\", lastName=\"%s\" LOCK RECORD";
    private static final String UPDATE_CONFIG_COMMAND = "update %s put configs = \"%s\",\"%s\" LOCK RECORD";
    private static final String UPDATE_IP_FIELD_COMMAND = "UPDATE %s SET ips = [] LOCK RECORD";
    private static final String ATTACH_IP_COMMAND = "update %s add ips = '%s' LOCK RECORD";
    private static final String UPDATE_PASSWORD_COMMAND = "update %s set passHash=\"%s\" LOCK RECORD";
    private static final String UPDATE_USERNAME_COMMAND = "update %s set username=\"%s\" LOCK RECORD";
    private static final String UPDATE_EMAIL_COMMAND = "update %s set email=\"%s\" LOCK RECORD";
    private static final String UPDATE_ROLE_MANAGEMENT_COMMAND = "update %s set roleManagement=%s LOCK RECORD";

    private static final String ALL_ACCOUNTS_METRIC = Metrics.name(AccountOrientDbRepository.class, "all_accounts");
    private static final String ACCOUNT_METRIC = Metrics.name(AccountOrientDbRepository.class, "account");
    private static final String ACCOUNT_TOKEN_METRIC = Metrics.name(AccountOrientDbRepository.class, "account", "token");
    private static final String ACCOUNT_INSERT_METRIC = Metrics.name(AccountOrientDbRepository.class, "account", "insert");
    private static final String ACCOUNT_UPDATE_CONFIG_METRIC = Metrics.name(AccountOrientDbRepository.class, "account", "update", "config");
    private static final String ACCOUNT_UPDATE_ROLES_METRIC = Metrics.name(AccountOrientDbRepository.class, "account", "update", "roles");
    private static final String ACCOUNT_UPDATE_INFO_METRIC = Metrics.name(AccountOrientDbRepository.class, "account", "update", "info");
    private static final String ACCOUNT_DELETE_METRIC = Metrics.name(AccountOrientDbRepository.class, "account", "delete");
    private static final String ACCOUNT_NEW_TOKEN_METRIC = Metrics.name(AccountOrientDbRepository.class, "account", "new_token");
    private static final String ACCOUNT_NEW_PASSWORD_METRIC = Metrics.name(AccountOrientDbRepository.class, "account", "new_password");
    private static final String ACCOUNT_NEW_USERNAME_METRIC = Metrics.name(AccountOrientDbRepository.class, "account", "new_username");
    private static final String ACCOUNT_NEW_EMAIL_METRIC = Metrics.name(AccountOrientDbRepository.class, "account", "new_email");
    private static final String ACCOUNT_CONFIRM_METRIC = Metrics.name(AccountOrientDbRepository.class, "account", "confirm");
}
