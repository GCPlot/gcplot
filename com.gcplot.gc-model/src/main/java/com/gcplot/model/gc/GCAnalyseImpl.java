package com.gcplot.model.gc;

import com.gcplot.Identifier;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class GCAnalyseImpl implements GCAnalyse {

    @Override
    public String id() {
        return id;
    }
    public GCAnalyseImpl id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public Identifier accountId() {
        return accountId;
    }
    public GCAnalyseImpl accountId(Identifier accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public String name() {
        return name;
    }
    public GCAnalyseImpl name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public boolean isContinuous() {
        return isContinuous;
    }
    public GCAnalyseImpl isContinuous(boolean isContinuous) {
        this.isContinuous = isContinuous;
        return this;
    }

    @Override
    public DateTime start() {
        return start;
    }
    public GCAnalyseImpl start(DateTime start) {
        this.start = start;
        return this;
    }

    @Override
    public DateTime lastEvent() {
        return lastEvent;
    }
    public GCAnalyseImpl lastEvent(DateTime lastEvent) {
        this.lastEvent = lastEvent;
        return this;
    }

    @Override
    public GarbageCollectorType collectorType() {
        return collectorType;
    }

    @Override
    public Map<String, String> jvmHeaders() {
        return jvmHeaders;
    }
    public GCAnalyseImpl jvmHeaders(Map<String, String> jvmHeaders) {
        this.jvmHeaders = jvmHeaders;
        return this;
    }

    public GCAnalyseImpl collectorType(GarbageCollectorType collectorType) {
        this.collectorType = collectorType;
        return this;
    }

    @Override
    public Set<String> jvmIds() {
        return Collections.unmodifiableSet(jvmIds);
    }
    public GCAnalyseImpl jvmIds(Set<String> jvmIds) {
        this.jvmIds = jvmIds;
        return this;
    }

    @Override
    public Map<String, MemoryDetails> jvmMemoryDetails() {
        return Collections.unmodifiableMap(jvmMemoryDetails);
    }
    public GCAnalyseImpl jvmMemoryDetails(Map<String, MemoryDetails> jvmMemoryDetails) {
        this.jvmMemoryDetails = jvmMemoryDetails;
        return this;
    }

    @Override
    public String ext() {
        return ext;
    }
    public GCAnalyseImpl ext(String ext) {
        this.ext = ext;
        return this;
    }

    protected String id;
    protected Identifier accountId;
    protected String name;
    protected boolean isContinuous;
    protected DateTime start;
    protected DateTime lastEvent;
    protected GarbageCollectorType collectorType;
    protected Map<String, String> jvmHeaders = Collections.emptyMap();
    protected Set<String> jvmIds = Collections.emptySet();
    protected Map<String, MemoryDetails> jvmMemoryDetails = Collections.emptyMap();
    protected String ext;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GCAnalyseImpl gcAnalyse = (GCAnalyseImpl) o;

        if (isContinuous != gcAnalyse.isContinuous) return false;
        if (id != null ? !id.equals(gcAnalyse.id) : gcAnalyse.id != null) return false;
        if (accountId != null ? !accountId.equals(gcAnalyse.accountId) : gcAnalyse.accountId != null) return false;
        if (name != null ? !name.equals(gcAnalyse.name) : gcAnalyse.name != null) return false;
        if (start != null ? !start.equals(gcAnalyse.start) : gcAnalyse.start != null) return false;
        if (lastEvent != null ? !lastEvent.equals(gcAnalyse.lastEvent) : gcAnalyse.lastEvent != null) return false;
        if (collectorType != gcAnalyse.collectorType) return false;
        if (jvmHeaders != null ? !jvmHeaders.equals(gcAnalyse.jvmHeaders) : gcAnalyse.jvmHeaders != null) return false;
        if (jvmIds != null ? !jvmIds.equals(gcAnalyse.jvmIds) : gcAnalyse.jvmIds != null) return false;
        return jvmMemoryDetails != null ? jvmMemoryDetails.equals(gcAnalyse.jvmMemoryDetails) : gcAnalyse.jvmMemoryDetails == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (accountId != null ? accountId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (isContinuous ? 1 : 0);
        result = 31 * result + (start != null ? start.hashCode() : 0);
        result = 31 * result + (lastEvent != null ? lastEvent.hashCode() : 0);
        result = 31 * result + (collectorType != null ? collectorType.hashCode() : 0);
        result = 31 * result + (jvmHeaders != null ? jvmHeaders.hashCode() : 0);
        result = 31 * result + (jvmIds != null ? jvmIds.hashCode() : 0);
        result = 31 * result + (jvmMemoryDetails != null ? jvmMemoryDetails.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GCAnalyseImpl{" +
                "id='" + id + '\'' +
                ", accountId=" + accountId +
                ", name='" + name + '\'' +
                ", isContinuous=" + isContinuous +
                ", start=" + start +
                ", lastEvent=" + lastEvent +
                ", collectorType=" + collectorType +
                ", jvmHeaders=" + jvmHeaders +
                ", jvmIds=" + jvmIds +
                ", jvmMemoryDetails=" + jvmMemoryDetails +
                ", ext='" + ext + '\'' +
                '}';
    }
    
}
