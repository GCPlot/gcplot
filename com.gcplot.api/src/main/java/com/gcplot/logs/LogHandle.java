package com.gcplot.logs;

import com.gcplot.Identifier;

import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/14/17
 */
public class LogHandle {
    public static final LogHandle INVALID_LOG = new LogHandle(null, null, null, null);
    private final String name;
    private final String username;
    private final String analyzeId;
    private final String jvmId;
    private final Map<String, String> properties;

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getAnalyzeId() {
        return analyzeId;
    }

    public String getJvmId() {
        return jvmId;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public LogHandle(String name, String username, String analyzeId, String jvmId) {
        this(name, username, analyzeId, jvmId, Collections.emptyMap());
    }

    public LogHandle(String name, String username, String analyzeId, String jvmId, Map<String, String> properties) {
        this.name = name;
        this.username = username;
        this.analyzeId = analyzeId;
        this.jvmId = jvmId;
        this.properties = Collections.unmodifiableMap(properties);
    }
}
