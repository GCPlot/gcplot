package com.gcplot.model.account;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/3/17
 */
public enum ConfigProperty implements com.gcplot.model.config.ConfigProperty  {

    PRELOAD_ANALYSIS_ON_PAGE_OPEN("1", true),

    /** Notification Configurations **/
    NOTIFICATIONS_ENABLED("2", true),
    NOTIFY_REALTIME_AGENT_HEALTH("3", true),
    REALTIME_AGENT_INACTIVE_SECONDS("4", 7200L);
    /*********************************/

    private static final Map<String, ConfigProperty> VALUES;

    static {
        HashMap<String, ConfigProperty> values = new HashMap<>();
        for (ConfigProperty cp : values()) {
            values.put(cp.getId(), cp);
        }
        VALUES = values;
    }

    private final String id;
    private final Object defaultValue;

    public String getId() {
        return id;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    ConfigProperty(String id, Object defaultValue) {
        this.id = id;
        this.defaultValue = defaultValue;
    }

    public static ConfigProperty by(String id) {
        return VALUES.get(id);
    }
}
