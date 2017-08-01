package com.gcplot.triggers;

import java.util.Map;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/1/17
 */
public interface Trigger<State> {

    TriggerType type();

    long lastTimeTrigger();

    State previousState();

    State state();

    Map<String, String> properties();

}
