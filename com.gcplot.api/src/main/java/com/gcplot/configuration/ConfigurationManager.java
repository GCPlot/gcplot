package com.gcplot.configuration;

import com.gcplot.commons.Configuration;

public interface ConfigurationManager {

    String readString(Configuration configuration);

    int readInt(Configuration configuration);

    long readLong(Configuration configuration);

    boolean readBoolean(Configuration configuration);

    double readDouble(Configuration configuration);

    void putProperty(Configuration key, Object value);

}
