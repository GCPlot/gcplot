package com.gcplot.model.gc.analysis;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 10/24/17
 */
public enum ConfigProperty implements com.gcplot.model.config.ConfigProperty {
    GRAPHITE_URLS("1", null),
    GRAPHITE_PREFIX("2", "${jvm_name}.gc."),
    GRAPHITE_PROXY_TYPE("3", 0L),
    GRAPHITE_PROXY_HOST("4", null),
    GRAPHITE_PROXY_PORT("5", 0L),
    GRAPHITE_PROXY_USERNAME("6", null),
    GRAPHITE_PROXY_PASSWORD("7", null);

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
