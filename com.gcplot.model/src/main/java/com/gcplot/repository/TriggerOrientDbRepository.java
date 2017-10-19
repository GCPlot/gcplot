package com.gcplot.repository;

import com.gcplot.Identifier;
import com.gcplot.commons.Metrics;
import com.gcplot.commons.exceptions.NotUniqueException;
import com.gcplot.triggers.AbstractTrigger;
import com.gcplot.triggers.BinaryTriggerImpl;
import com.gcplot.triggers.Trigger;
import com.gcplot.utils.Exceptions;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePoolFactory;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        register(db, db.getMetadata().getSchema(), DummyTrigger.class);
    }

    @Override
    public List<Trigger> triggersFor(Identifier accountId) {
        metrics.meter(TRIGGERS_METRIC).mark();
        try (OObjectDatabaseTx db = db()) {
            List<Trigger> l = db.query(new OSQLSynchQuery<>(String.format(TRIGGERS_QUERY, accountId)));
            return detachList(db, l).collect(Collectors.toList());
        }
    }

    @Override
    public Trigger trigger(Identifier triggerId) {
        metrics.meter(TRIGGER_METRIC).mark();
        try (OObjectDatabaseTx db = db()) {
            List<Trigger> l = db.query(new OSQLSynchQuery<>(String.format(TRIGGER_QUERY, triggerId)));
            return detachList(db, l).findFirst().orElse(null);
        }
    }

    @Override
    public <T extends Trigger> T saveTrigger(T trigger) {
        metrics.meter(SAVE_TRIGGER_METRIC).mark();
        try (OObjectDatabaseTx db = db()) {
            try {
                if (db.getMetadata().getSchema().getClass(trigger.getClass()) == null) {
                    register(db, db.getMetadata().getSchema(), trigger.getClass());
                }
                return db.detachAll(db.save(trigger), true);
            } catch (ORecordDuplicatedException e) {
                throw new NotUniqueException(e.getMessage());
            }
        }
    }

    @Override
    public <T> void updateState(Identifier triggerId, T state) {
        metrics.meter(UPDATE_STATE_METRIC).mark();
        updateTrigger(triggerId, t -> t.setState(state));
    }

    @Override
    public void updateLastTimeTriggered(Identifier triggerId, long lastTimeTriggered) {
        metrics.meter(UPDATE_LAST_TIME_TRIGGERED_METRIC).mark();
        updateTrigger(triggerId, t -> t.setLastTimeTrigger(lastTimeTriggered));
    }

    @Override
    public void putProperty(Identifier triggerId, String key, String value) {
        metrics.meter(PUT_PROPERTY_METRIC).mark();
        updateTrigger(triggerId, t -> {
            if (t.properties() == null) {
                t.setProperties(new HashMap<>());
            }
            t.properties().put(key, value);
        });
    }

    private void updateTrigger(Identifier triggerId, Consumer<AbstractTrigger> c) {
        try (OObjectDatabaseTx db = db()) {
            try {
                db.begin();
                List<AbstractTrigger> l = db.query(new OSQLSynchQuery<>(String.format(TRIGGER_QUERY, triggerId)));
                if (l.size() != 1) {
                    throw new IllegalArgumentException("Can't find trigger with id=" + triggerId);
                } else {
                    AbstractTrigger trigger = l.get(0);
                    c.accept(trigger);
                    db.save(trigger);
                }
            } catch (Throwable t) {
                db.rollback();
                throw Exceptions.runtime(t);
            } finally {
                db.commit();
            }
        }
    }

    private Stream<Trigger> detachList(OObjectDatabaseTx db, List<Trigger> l) {
        return l.stream().map(i -> (Trigger) db.detachAll(i, true));
    }

    @Override
    public void removeProperty(Identifier triggerId, String key) {
        metrics.meter(REMOVE_PROPERTY_METRIC).mark();
    }

    private static final String DOCUMENT_NAME = AbstractTrigger.class.getSimpleName();
    private static final String TRIGGERS_QUERY = "select from " + DOCUMENT_NAME + " where accountId = '%s'";
    private static final String TRIGGER_QUERY = "select from %s";
    private static final String TRIGGER_METRIC = Metrics.name(TriggerOrientDbRepository.class, "trigger");
    private static final String TRIGGERS_METRIC = Metrics.name(TriggerOrientDbRepository.class, "triggers_for");
    private static final String SAVE_TRIGGER_METRIC = Metrics.name(TriggerOrientDbRepository.class, "save_trigger");
    private static final String UPDATE_STATE_METRIC = Metrics.name(TriggerOrientDbRepository.class, "update_state");
    private static final String UPDATE_LAST_TIME_TRIGGERED_METRIC = Metrics.name(TriggerOrientDbRepository.class, "update_last_time_triggered");
    private static final String PUT_PROPERTY_METRIC = Metrics.name(TriggerOrientDbRepository.class, "put_property");
    private static final String REMOVE_PROPERTY_METRIC = Metrics.name(TriggerOrientDbRepository.class, "remove_property");

    /**
     * hack in order to have AbstractTrigger as a registered entity
     */
    public static class DummyTrigger extends AbstractTrigger {

        @Override
        public Object state() {
            return null;
        }
    }
}
