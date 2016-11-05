package com.gcplot.model.gc;

import com.gcplot.Identifier;
import com.gcplot.model.VMVersion;
import com.google.common.base.MoreObjects;
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
    public String timezone() {
        return timezone;
    }
    public GCAnalyseImpl timezone(String timezone) {
        this.timezone = timezone;
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
    public Map<String, String> jvmHeaders() {
        return Collections.unmodifiableMap(jvmHeaders);
    }
    public GCAnalyseImpl jvmHeaders(Map<String, String> jvmHeaders) {
        this.jvmHeaders = jvmHeaders;
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
    public Map<String, String> jvmNames() {
        return Collections.unmodifiableMap(jvmNames);
    }
    public GCAnalyseImpl jvmNames(Map<String, String> jvmNames) {
        this.jvmNames = jvmNames;
        return this;
    }

    @Override
    public Map<String, VMVersion> jvmVersions() {
        return Collections.unmodifiableMap(jvmVersions);
    }
    public GCAnalyseImpl jvmVersions(Map<String, VMVersion> jvmVersions) {
        this.jvmVersions = jvmVersions;
        return this;
    }

    @Override
    public Map<String, GarbageCollectorType> jvmGCTypes() {
        return Collections.unmodifiableMap(jvmGCTypes);
    }
    public GCAnalyseImpl jvmGCTypes(Map<String, GarbageCollectorType> jvmGCTypes) {
        this.jvmGCTypes = jvmGCTypes;
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

    public GCAnalyseImpl() {
    }

    public GCAnalyseImpl(GCAnalyse other) {
        this.id = other.id();
        this.accountId = other.accountId();
        this.name = other.name();
        this.isContinuous = other.isContinuous();
        this.start = other.start();
        this.lastEvent = other.lastEvent();
        this.jvmHeaders = other.jvmHeaders();
        this.jvmIds = other.jvmIds();
        this.jvmNames = other.jvmNames();
        this.jvmVersions = other.jvmVersions();
        this.jvmGCTypes = other.jvmGCTypes();
        this.jvmMemoryDetails = other.jvmMemoryDetails();
        this.ext = other.ext();
    }

    protected String id;
    protected Identifier accountId;
    protected String name;
    protected String timezone;
    protected boolean isContinuous;
    protected DateTime start;
    protected DateTime lastEvent;
    protected Map<String, String> jvmHeaders = Collections.emptyMap();
    protected Set<String> jvmIds = Collections.emptySet();
    protected Map<String, String> jvmNames = Collections.emptyMap();
    protected Map<String, VMVersion> jvmVersions = Collections.emptyMap();
    protected Map<String, GarbageCollectorType> jvmGCTypes = Collections.emptyMap();
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
        if (timezone != null ? !timezone.equals(gcAnalyse.timezone) : gcAnalyse.timezone != null) return false;
        if (start != null ? !start.equals(gcAnalyse.start) : gcAnalyse.start != null) return false;
        if (lastEvent != null ? !lastEvent.equals(gcAnalyse.lastEvent) : gcAnalyse.lastEvent != null) return false;
        if (jvmHeaders != null ? !jvmHeaders.equals(gcAnalyse.jvmHeaders) : gcAnalyse.jvmHeaders != null) return false;
        if (jvmIds != null ? !jvmIds.equals(gcAnalyse.jvmIds) : gcAnalyse.jvmIds != null) return false;
        if (jvmNames != null ? !jvmNames.equals(gcAnalyse.jvmNames) : gcAnalyse.jvmNames != null) return false;
        if (jvmVersions != null ? !jvmVersions.equals(gcAnalyse.jvmVersions) : gcAnalyse.jvmVersions != null)
            return false;
        if (jvmGCTypes != null ? !jvmGCTypes.equals(gcAnalyse.jvmGCTypes) : gcAnalyse.jvmGCTypes != null) return false;
        if (jvmMemoryDetails != null ? !jvmMemoryDetails.equals(gcAnalyse.jvmMemoryDetails) : gcAnalyse.jvmMemoryDetails != null)
            return false;
        return ext != null ? ext.equals(gcAnalyse.ext) : gcAnalyse.ext == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (accountId != null ? accountId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (timezone != null ? timezone.hashCode() : 0);
        result = 31 * result + (isContinuous ? 1 : 0);
        result = 31 * result + (start != null ? start.hashCode() : 0);
        result = 31 * result + (lastEvent != null ? lastEvent.hashCode() : 0);
        result = 31 * result + (jvmHeaders != null ? jvmHeaders.hashCode() : 0);
        result = 31 * result + (jvmIds != null ? jvmIds.hashCode() : 0);
        result = 31 * result + (jvmNames != null ? jvmNames.hashCode() : 0);
        result = 31 * result + (jvmVersions != null ? jvmVersions.hashCode() : 0);
        result = 31 * result + (jvmGCTypes != null ? jvmGCTypes.hashCode() : 0);
        result = 31 * result + (jvmMemoryDetails != null ? jvmMemoryDetails.hashCode() : 0);
        result = 31 * result + (ext != null ? ext.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("GCAnalyseImpl{");
        sb.append("id='").append(id).append('\'');
        sb.append(", accountId=").append(accountId);
        sb.append(", name='").append(name).append('\'');
        sb.append(", timezone='").append(timezone).append('\'');
        sb.append(", isContinuous=").append(isContinuous);
        sb.append(", start=").append(start);
        sb.append(", lastEvent=").append(lastEvent);
        sb.append(", jvmHeaders=").append(jvmHeaders);
        sb.append(", jvmIds=").append(jvmIds);
        sb.append(", jvmNames=").append(jvmNames);
        sb.append(", jvmVersions=").append(jvmVersions);
        sb.append(", jvmGCTypes=").append(jvmGCTypes);
        sb.append(", jvmMemoryDetails=").append(jvmMemoryDetails);
        sb.append(", ext='").append(ext).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
