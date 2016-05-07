package com.gcplot.configuration;

public interface ConfigurationManager {

    String readString(String key);

    String readString(String key, String defaultValue);

    Integer readInt(String key);

    int readInt(String key, int defaultValue);

    Long readLong(String key);

    long readLong(String key, long defaultValue);

    Boolean readBoolean(String key);

    boolean readBoolean(String key, boolean defaultValue);

    Double readDouble(String key);

    double readDouble(String key, double defaultValue);

    void putProperty(String key, Object value);

}
