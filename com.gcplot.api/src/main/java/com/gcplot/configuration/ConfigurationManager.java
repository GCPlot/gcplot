package com.gcplot.configuration;

public interface ConfigurationManager {

    Object readProperty(String key);

    String readString(String key);

    int readInt(String key);

    long readLong(String key);

    boolean readBoolean(String key);

    double readDouble(String key);

    void putProperty(String key, Object value);

}
