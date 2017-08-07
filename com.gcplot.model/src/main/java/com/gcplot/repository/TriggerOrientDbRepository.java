package com.gcplot.repository;

import com.gcplot.Identifier;
import com.gcplot.commons.Metrics;
import com.gcplot.commons.exceptions.NotUniqueException;
import com.gcplot.triggers.AbstractTrigger;
import com.gcplot.triggers.Trigger;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePoolFactory;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 8/2/17
 */
public class TriggerOrientDbRepository extends AbstractOrientDbRepository implements TriggerRepository {

    public TriggerOrientDbRepository(OrientDbConfig config, OPartitionedDatabasePoolFactory poolFactory) {
        super(config, poolFactory);
    }

    @Override
    protected void init(OObjectDatabaseTx db, OSchema schema) {
    }

    @Override
    public List<Trigger> triggersFor(Identifier accountId) {
        metrics.meter(TRIGGERS_METRIC).mark();
        try (OObjectDatabaseTx db = db()) {
            List<Trigger> l = db.query(new OSQLSynchQuery<>(String.format(TRIGGERS_QUERY, accountId)));
            return l.stream().map(i -> (Trigger) db.detachAll(i, true)).collect(Collectors.toList());
        }
    }

    @Override
    public void saveTrigger(Trigger trigger) {
        metrics.meter(SAVE_TRIGGER_METRIC).mark();
        try (OObjectDatabaseTx db = db()) {
            try {
                if (db.getMetadata().getSchema().getClass(trigger.getClass()) == null) {
                    register(db, db.getMetadata().getSchema(), trigger.getClass());
                }
                db.detachAll(db.save(trigger), true);
            } catch (ORecordDuplicatedException e) {
                throw new NotUniqueException(e.getMessage());
            }
        }
    }

    @Override
    public <T> void updateState(Identifier triggerId, T state) {
        metrics.meter(UPDATE_STATE_METRIC).mark();
    }

    @Override
    public void updateLastTimeTriggered(Identifier triggerId, long lastTimeTriggered) {
        metrics.meter(UPDATE_LAST_TIME_TRIGGERED_METRIC).mark();
    }

    @Override
    public void putProperty(Identifier triggerId, String key, String value) {
        metrics.meter(PUT_PROPERTY_METRIC).mark();
    }

    @Override
    public void removeProperty(Identifier triggerId, String key) {
        metrics.meter(REMOVE_PROPERTY_METRIC).mark();
    }

    private static final String DOCUMENT_NAME = AbstractTrigger.class.getSimpleName();
    private static final String TRIGGERS_QUERY = "select from " + DOCUMENT_NAME + " where accountId = '%s'";
    private static final String TRIGGERS_METRIC = Metrics.name(TriggerOrientDbRepository.class, "triggers_for");
    private static final String SAVE_TRIGGER_METRIC = Metrics.name(TriggerOrientDbRepository.class, "save_trigger");
    private static final String UPDATE_STATE_METRIC = Metrics.name(TriggerOrientDbRepository.class, "update_state");
    private static final String UPDATE_LAST_TIME_TRIGGERED_METRIC = Metrics.name(TriggerOrientDbRepository.class, "update_last_time_triggered");
    private static final String PUT_PROPERTY_METRIC = Metrics.name(TriggerOrientDbRepository.class, "put_property");
    private static final String REMOVE_PROPERTY_METRIC = Metrics.name(TriggerOrientDbRepository.class, "remove_property");
}
