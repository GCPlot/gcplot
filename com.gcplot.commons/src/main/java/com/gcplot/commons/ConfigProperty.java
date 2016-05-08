package com.gcplot.commons;

public enum ConfigProperty {

    POLL_INTERVAL("config.poll.interval", 15000),
    TEST1_CONFIG("test.config.1", "test");

    private String key;
    public String getKey() {
        return key;
    }

    private Object defaultValue;
    public Object getDefaultValue() {
        return defaultValue;
    }

    ConfigProperty(String key, Object defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

}
