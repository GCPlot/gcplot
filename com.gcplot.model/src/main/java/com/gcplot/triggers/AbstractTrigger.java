package com.gcplot.triggers;

import javax.persistence.Id;
import java.util.Map;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/1/17
 */
public abstract class AbstractTrigger {
    @Id
    private int id;
    private transient TriggerType triggerType;
    private long lastTimeTrigger;
    private Map<String, String> properties;

    public int getId() {
        return id;
    }

    public TriggerType type() {
        if (triggerType == null) {
            triggerType = TriggerType.by(id);
        }
        return triggerType;
    }

    public long lastTimeTrigger() {
        return lastTimeTrigger;
    }

    public Map<String, String> properties() {
        return properties;
    }

}
