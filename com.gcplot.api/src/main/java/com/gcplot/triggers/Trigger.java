package com.gcplot.triggers;

import com.gcplot.Identifier;

import java.util.Map;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/1/17
 */
public interface Trigger<State> {

    Identifier id();

    Identifier accountId();

    TriggerType type();

    long lastTimeTrigger();

    State state();

    Map<String, String> properties();

}
