package com.gcplot.repository;

import com.gcplot.Identifier;
import com.gcplot.commons.Metrics;
import com.gcplot.commons.exceptions.NotUniqueException;
import com.gcplot.model.role.Role;
import com.gcplot.model.role.RoleImpl;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePoolFactory;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/14/16
 */
public class RolesOrientDbRepository extends AbstractOrientDbRepository implements RolesRepository {

    public RolesOrientDbRepository(OrientDbConfig config, OPartitionedDatabasePoolFactory poolFactory) {
        super(config, poolFactory);
    }

    @Override
    protected void init(OObjectDatabaseTx db, OSchema schema) {
        if (schema.getClass(ROLES_DOCUMENT_NAME) == null) {
            db.getEntityManager().registerEntityClass(RoleImpl.RestrictionImpl.class);
            db.getEntityManager().registerEntityClass(RoleImpl.class);
        }
    }

    @Override
    public List<Role> roles() {
        metrics.meter(ALL_ROLES_METRIC).mark();
        try (OObjectDatabaseTx db = db()) {
            List<Role> l = db.query(new OSQLSynchQuery<>(ALL_ROLES_QUERY));
            return l.stream().map(i -> (RoleImpl) db.detachAll(i, true)).collect(Collectors.toList());
        }
    }

    @Override
    public Optional<Role> role(Identifier id) {
        metrics.meter(SINGLE_ROLE_METRIC).mark();
        return get(id);
    }

    @Override
    public Role store(Role role) {
        metrics.meter(ROLE_STORE_METRIC).mark();
        try (OObjectDatabaseTx db = db()) {
            try {
                return db.detachAll(db.save(role), true);
            } catch (ORecordDuplicatedException e) {
                throw new NotUniqueException(e.getMessage());
            }
        }
    }

    @Override
    public void delete(Role role) {
        metrics.meter(ROLE_DELETE_METRIC).mark();
        try (OObjectDatabaseTx db = db()) {
            db.delete((ORID) ((RoleImpl) role).getOId());
        }
    }

    protected static final Logger LOG = LoggerFactory.getLogger(RolesOrientDbRepository.class);
    private static final String ROLES_DOCUMENT_NAME = RoleImpl.class.getSimpleName();
    private static final String ALL_ROLES_QUERY = "select from " + ROLES_DOCUMENT_NAME;

    private static final String ALL_ROLES_METRIC = Metrics.name(RolesOrientDbRepository.class, "all_roles");
    private static final String SINGLE_ROLE_METRIC = Metrics.name(RolesOrientDbRepository.class, "role");
    private static final String ROLE_STORE_METRIC = Metrics.name(RolesOrientDbRepository.class, "store");
    private static final String ROLE_DELETE_METRIC = Metrics.name(RolesOrientDbRepository.class, "delete");
}
