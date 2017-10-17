package com.gcplot.services.triggers.strategy;

import com.gcplot.model.account.Account;
import com.gcplot.model.account.config.ConfigProperty;
import com.gcplot.model.gc.GCAnalyse;
import com.gcplot.repository.GCAnalyseRepository;
import com.gcplot.services.mail.MailService;
import com.gcplot.services.mail.data.JvmAgentStatus;
import com.gcplot.triggers.Trigger;
import com.gcplot.triggers.TriggerType;
import com.gcplot.triggers.binary.BinaryTrigger;
import com.gcplot.triggers.binary.State;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Seconds;

import java.util.*;
import java.util.stream.Collectors;

import static com.gcplot.utils.CollectionUtils.map;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 9/6/17
 */
public class RealtimeAgentHealthStrategy extends BaseTriggerStrategy<List<GCAnalyse>> {
    private static final String JVM_ID_PROP = "jvm_id";
    private GCAnalyseRepository analyseRepository;
    private MailService mailService;

    @Override
    public TriggerStrategyType type() {
        return TriggerStrategyType.REALTIME_ANALYSIS;
    }

    @Override
    public boolean isActive(Account account) {
        return account.config().asBoolean(ConfigProperty.NOTIFY_REALTIME_AGENT_HEALTH);
    }

    @Override
    public void process(Account account, List<GCAnalyse> analyses, List<Trigger> triggers) {
        DateTime now = DateTime.now(DateTimeZone.UTC);
        long maxInactiveSeconds = account.config().asLong(ConfigProperty.REALTIME_AGENT_INACTIVE_SECONDS);
        Map<String, BinaryTrigger> triggerMap = triggers.stream()
                .filter(t -> t.type() == TriggerType.REALTIME_AGENT_HEALTH)
                .map(BinaryTrigger.class::cast)
                .collect(Collectors.toMap(k -> k.properties().get(JVM_ID_PROP), v -> v));

        List<JvmAgentStatus> statuses = new ArrayList<>();
        analyses.forEach(analysis -> {
            if (analysis.jvmIds().size() != 0) {
                Set<Pair<String, Long>> jvmStatuses = new HashSet<>();
                analysis.jvmIds().forEach(jvm -> {
                    DateTime dt = analysis.lastEvent().get(jvm);
                    if (dt != null) {
                        long inactiveSeconds = Seconds.secondsBetween(dt, now).getSeconds();
                        boolean isBroken = inactiveSeconds > maxInactiveSeconds;
                        BinaryTrigger trigger = triggerMap.get(jvm);
                        if (trigger == null) {
                            trigger = getTriggerRepository().saveTrigger(
                                    getTriggerFactory().createBinary(account.id(), State.WAIT, map(JVM_ID_PROP, jvm)));
                        }
                        if (trigger.state() == State.ACQUIRED && !isBroken) {
                            getTriggerRepository().updateState(trigger.id(), State.WAIT);
                        } else if (trigger.state() == State.WAIT && isBroken) {
                            getTriggerRepository().updateState(trigger.id(), State.ACQUIRED);
                            if (isEmailEnabled()) {
                                jvmStatuses.add(Pair.of(analysis.jvmNames().get(jvm), inactiveSeconds));
                            }
                        }
                    }
                });
                if (jvmStatuses.size() > 0) {
                    statuses.add(new JvmAgentStatus(analysis, jvmStatuses));
                }
            }
        });
        if (statuses.size() > 0) {
            mailService.sendRealtimeAgentHealth(account, statuses);
        }
    }

    private boolean isEmailEnabled() {
        return getConfig().readBoolean(com.gcplot.configuration.ConfigProperty.TRIGGERS_EMAIL_ENABLED);
    }

    public GCAnalyseRepository getAnalyseRepository() {
        return analyseRepository;
    }

    public void setAnalyseRepository(GCAnalyseRepository analyseRepository) {
        this.analyseRepository = analyseRepository;
    }

    public MailService getMailService() {
        return mailService;
    }

    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }
}
