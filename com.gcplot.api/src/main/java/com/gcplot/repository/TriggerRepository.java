package com.gcplot.repository;

import com.gcplot.Identifier;
import com.gcplot.triggers.Trigger;

import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 8/2/17
 */
public interface TriggerRepository {

    List<Trigger> triggersFor(Identifier accountId);

    void saveTrigger(Trigger trigger);

    <T> void updateState(Identifier triggerId, T state);

    void updateLastTimeTriggered(Identifier triggerId, long lastTimeTriggered);

    void putProperty(Identifier triggerId, String key, String value);

    void removeProperty(Identifier triggerId, String key);

}
