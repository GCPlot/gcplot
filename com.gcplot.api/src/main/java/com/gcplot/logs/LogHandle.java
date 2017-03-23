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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogHandle logHandle = (LogHandle) o;

        if (name != null ? !name.equals(logHandle.name) : logHandle.name != null) return false;
        if (username != null ? !username.equals(logHandle.username) : logHandle.username != null) return false;
        if (analyzeId != null ? !analyzeId.equals(logHandle.analyzeId) : logHandle.analyzeId != null) return false;
        if (jvmId != null ? !jvmId.equals(logHandle.jvmId) : logHandle.jvmId != null) return false;
        if (properties != null ? !properties.equals(logHandle.properties) : logHandle.properties != null) return false;
        return hash != null ? hash.equals(logHandle.hash) : logHandle.hash == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (analyzeId != null ? analyzeId.hashCode() : 0);
        result = 31 * result + (jvmId != null ? jvmId.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        result = 31 * result + (hash != null ? hash.hashCode() : 0);
        return result;
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
