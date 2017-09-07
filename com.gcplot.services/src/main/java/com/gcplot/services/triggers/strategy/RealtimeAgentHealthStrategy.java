package com.gcplot.services.triggers.strategy;

import com.gcplot.model.account.Account;
import com.gcplot.model.account.config.ConfigProperty;
import com.gcplot.triggers.Trigger;
import com.gcplot.triggers.TriggerType;
import com.gcplot.triggers.binary.BinaryTrigger;
import com.gcplot.triggers.binary.State;

import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 9/6/17
 */
public class RealtimeAgentHealthStrategy extends BaseTriggerStrategy {

    @Override
    public boolean isActive(Account account) {
        return account.config().asBoolean(ConfigProperty.NOTIFY_REALTIME_AGENT_HEALTH);
    }

    @Override
    public void process(Account account, List<Trigger> triggers) {
        BinaryTrigger trigger = (BinaryTrigger) triggers.stream()
                .filter(t -> t.type() == TriggerType.REALTIME_AGENT_HEALTH).findFirst().orElse(null);
        if (trigger == null) {
            trigger = getTriggerFactory().createBinary(account.id(), State.WAIT);
            getTriggerRepository().saveTrigger(trigger);
        } else {

        }
    }
}
