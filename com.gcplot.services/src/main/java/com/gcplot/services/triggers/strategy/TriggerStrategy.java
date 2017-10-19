package com.gcplot.services.triggers.strategy;

import com.gcplot.model.account.Account;
import com.gcplot.triggers.Trigger;

import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 9/5/17
 */
public interface TriggerStrategy<T> {

    TriggerStrategyType type();

    boolean isActive(Account account);

    void process(Account account, T data, List<Trigger> triggers);

}
