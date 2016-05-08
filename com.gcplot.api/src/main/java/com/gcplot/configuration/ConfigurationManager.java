package com.gcplot.configuration;

import com.gcplot.commons.ConfigProperty;

public interface ConfigurationManager {

    String readString(ConfigProperty configProperty);

    int readInt(ConfigProperty configProperty);

    long readLong(ConfigProperty configProperty);

    boolean readBoolean(ConfigProperty configProperty);

    double readDouble(ConfigProperty configProperty);

    void putProperty(ConfigProperty key, Object value);

}
