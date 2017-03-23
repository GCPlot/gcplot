package com.gcplot.logs;

import com.gcplot.Identifier;
import com.google.common.hash.Hashing;

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
    private final String hash;

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
        this.hash = Hashing.sha256().hashBytes((name + username + analyzeId + jvmId).getBytes()).toString();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("LogHandle{");
        sb.append("name='").append(name).append('\'');
        sb.append(", username='").append(username).append('\'');
        sb.append(", analyzeId='").append(analyzeId).append('\'');
        sb.append(", jvmId='").append(jvmId).append('\'');
        sb.append(", properties=").append(properties);
        sb.append(", hash='").append(hash).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String hash() {
        return hash;
    }
}
