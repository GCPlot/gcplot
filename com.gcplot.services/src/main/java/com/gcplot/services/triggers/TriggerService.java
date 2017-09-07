package com.gcplot.services.triggers;

import com.gcplot.model.account.Account;
import com.gcplot.repository.TriggerRepository;
import com.gcplot.services.triggers.strategy.TriggerStrategy;
import com.gcplot.triggers.Trigger;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 9/5/17
 */
public class TriggerService {
    private final TriggerRepository triggerRepository;
    private final List<TriggerStrategy> strategies;

    public TriggerService(TriggerRepository triggerRepository, List<TriggerStrategy> strategies) {
        this.triggerRepository = triggerRepository;
        this.strategies = Collections.unmodifiableList(strategies);
    }

    public void processAccountTriggers(Account account) {
        List<Trigger> triggers = triggerRepository.triggersFor(account.id());
        strategies.forEach(s -> {
            if (s.isActive(account)) {
                s.process(account, triggers);
            }
        });
    }

}
