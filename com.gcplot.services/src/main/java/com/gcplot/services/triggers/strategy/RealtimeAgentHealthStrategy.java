package com.gcplot.services.triggers.strategy;

import com.gcplot.model.account.Account;
import com.gcplot.model.account.config.ConfigProperty;
import com.gcplot.repository.GCAnalyseRepository;
import com.gcplot.triggers.Trigger;
import com.gcplot.triggers.TriggerType;
import com.gcplot.triggers.binary.BinaryTrigger;
import com.gcplot.triggers.binary.State;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Seconds;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gcplot.utils.CollectionUtils.map;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 9/6/17
 */
public class RealtimeAgentHealthStrategy extends BaseTriggerStrategy {
    private static final String JVM_ID_PROP = "jvm_id";
    private GCAnalyseRepository analyseRepository;

    @Override
    public boolean isActive(Account account) {
        return account.config().asBoolean(ConfigProperty.NOTIFY_REALTIME_AGENT_HEALTH);
    }

    @Override
    public void process(Account account, List<Trigger> triggers) {
        DateTime now = DateTime.now(DateTimeZone.UTC);
        long inactiveSeconds = account.config().asLong(ConfigProperty.REALTIME_AGENT_INACTIVE_SECONDS);
        Map<String, BinaryTrigger> triggerMap = triggers.stream()
                .filter(t -> t.type() == TriggerType.REALTIME_AGENT_HEALTH)
                .map(BinaryTrigger.class::cast)
                .collect(Collectors.toMap(k -> k.properties().get(JVM_ID_PROP), v -> v));

        analyseRepository.analysesFor(account.id()).forEach(a -> {
            if (a.isContinuous() && a.jvmIds().size() != 0) {
                a.jvmIds().forEach(jvm -> {
                    DateTime dt = a.lastEvent().get(jvm);
                    if (dt != null) {
                        boolean isBroken = Seconds.secondsBetween(dt, now).getSeconds() > inactiveSeconds;
                        BinaryTrigger trigger = triggerMap.get(jvm);
                        if (trigger == null) {
                            trigger = getTriggerRepository().saveTrigger(
                                    getTriggerFactory().createBinary(account.id(), State.WAIT, map(JVM_ID_PROP, jvm)));
                        }
                        if (trigger.state() == State.ACQUIRED && !isBroken) {
                            getTriggerRepository().updateState(trigger.id(), State.WAIT);
                        } else if (trigger.state() == State.WAIT && isBroken) {
                            getTriggerRepository().updateState(trigger.id(), );
                        }
                    }
                });
            }
        });
    }

    public GCAnalyseRepository getAnalyseRepository() {
        return analyseRepository;
    }

    public void setAnalyseRepository(GCAnalyseRepository analyseRepository) {
        this.analyseRepository = analyseRepository;
    }
}
