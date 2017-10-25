package com.gcplot.model.gc;

import com.gcplot.Identifier;
import com.gcplot.model.config.Configuration;
import com.gcplot.model.gc.analysis.ConfigProperty;
import com.gcplot.model.gc.analysis.GCAnalyse;
import com.gcplot.utils.Utils;
import com.gcplot.model.VMVersion;
import org.joda.time.DateTime;

import javax.persistence.Transient;
import java.util.*;

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
    public Map<String, DateTime> firstEvent() {
        return firstEvent;
    }
    public GCAnalyseImpl firstEvent(Map<String, DateTime> firstEvent) {
        this.firstEvent = firstEvent;
        return this;
    }

    @Override
    public Map<String, DateTime> lastEvent() {
        return lastEvent;
    }
    public GCAnalyseImpl lastEvent(Map<String, DateTime> lastEvent) {
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
    public SourceType sourceType() {
        return sourceType;
    }
    public GCAnalyseImpl sourceType(SourceType sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    @Override
    public Properties sourceConfigProps() {
        return sourceConfigProps;
    }
    @Override
    public String sourceConfig() {
        return sourceConfig;
    }
    public GCAnalyseImpl sourceConfig(String sourceConfig) {
        this.sourceConfig = sourceConfig;
        this.sourceConfigProps = Utils.fromString(sourceConfig);
        return this;
    }

    @Override
    public Map<String, SourceType> sourceByJvm() {
        return Collections.unmodifiableMap(sourceByJvm);
    }
    public GCAnalyseImpl sourceByJvm(Map<String, SourceType> sourceByJvm) {
        this.sourceByJvm = sourceByJvm;
        return this;
    }

    @Override
    public Map<String, String> sourceConfigByJvm() {
        return Collections.unmodifiableMap(sourceConfigByJvm);
    }
    public GCAnalyseImpl sourceConfigByJvm(Map<String, String> sourceConfigByJvm) {
        this.sourceConfigByJvm = sourceConfigByJvm;
        return this;
    }

    @Override
    public Configuration<ConfigProperty> config() {
        if (configuration == null) {
            configuration = Configuration.create(getConfigs());
        }
        return configuration;
    }

    @Override
    public String ext() {
        return ext;
    }
    public GCAnalyseImpl ext(String ext) {
        this.ext = ext;
        return this;
    }

    public Map<String, String> getConfigs() {
        if (configs == null) {
            configs = new HashMap<>();
        }
        return configs;
    }

    public GCAnalyseImpl() {
        this.configs = new HashMap<>();
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
        this.sourceType = other.sourceType();
        this.sourceConfig = other.sourceConfig();
        this.sourceByJvm = other.sourceByJvm();
        this.sourceConfigByJvm = other.sourceConfigByJvm();
        this.configs = ((GCAnalyseImpl)other).configs;
        this.ext = other.ext();
    }

    @Transient
    protected transient Configuration<ConfigProperty> configuration;
    protected String id;
    protected Identifier accountId;
    protected String name;
    protected String timezone;
    protected boolean isContinuous;
    protected DateTime start;
    protected Map<String, DateTime> firstEvent;
    protected Map<String, DateTime> lastEvent;
    protected Map<String, String> jvmHeaders = Collections.emptyMap();
    protected Set<String> jvmIds = Collections.emptySet();
    protected Map<String, String> jvmNames = Collections.emptyMap();
    protected Map<String, VMVersion> jvmVersions = Collections.emptyMap();
    protected Map<String, GarbageCollectorType> jvmGCTypes = Collections.emptyMap();
    protected Map<String, MemoryDetails> jvmMemoryDetails = Collections.emptyMap();
    protected SourceType sourceType;
    protected String sourceConfig;
    protected Properties sourceConfigProps;
    protected Map<String, SourceType> sourceByJvm = Collections.emptyMap();
    protected Map<String, String> sourceConfigByJvm = Collections.emptyMap();
    private Map<String, String> configs;
    protected String ext;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GCAnalyseImpl analyse = (GCAnalyseImpl) o;

        if (isContinuous != analyse.isContinuous) return false;
        if (id != null ? !id.equals(analyse.id) : analyse.id != null) return false;
        if (accountId != null ? !accountId.equals(analyse.accountId) : analyse.accountId != null) return false;
        if (name != null ? !name.equals(analyse.name) : analyse.name != null) return false;
        if (timezone != null ? !timezone.equals(analyse.timezone) : analyse.timezone != null) return false;
        if (start != null ? !start.equals(analyse.start) : analyse.start != null) return false;
        if (firstEvent != null ? !firstEvent.equals(analyse.firstEvent) : analyse.firstEvent != null) return false;
        if (lastEvent != null ? !lastEvent.equals(analyse.lastEvent) : analyse.lastEvent != null) return false;
        if (jvmHeaders != null ? !jvmHeaders.equals(analyse.jvmHeaders) : analyse.jvmHeaders != null) return false;
        if (jvmIds != null ? !jvmIds.equals(analyse.jvmIds) : analyse.jvmIds != null) return false;
        if (jvmNames != null ? !jvmNames.equals(analyse.jvmNames) : analyse.jvmNames != null) return false;
        if (jvmVersions != null ? !jvmVersions.equals(analyse.jvmVersions) : analyse.jvmVersions != null) return false;
        if (jvmGCTypes != null ? !jvmGCTypes.equals(analyse.jvmGCTypes) : analyse.jvmGCTypes != null) return false;
        if (jvmMemoryDetails != null ? !jvmMemoryDetails.equals(analyse.jvmMemoryDetails) : analyse.jvmMemoryDetails != null)
            return false;
        if (sourceType != analyse.sourceType) return false;
        if (sourceConfig != null ? !sourceConfig.equals(analyse.sourceConfig) : analyse.sourceConfig != null)
            return false;
        if (sourceByJvm != null ? !sourceByJvm.equals(analyse.sourceByJvm) : analyse.sourceByJvm != null) return false;
        if (sourceConfigByJvm != null ? !sourceConfigByJvm.equals(analyse.sourceConfigByJvm) : analyse.sourceConfigByJvm != null)
            return false;
        return ext != null ? ext.equals(analyse.ext) : analyse.ext == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (accountId != null ? accountId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (timezone != null ? timezone.hashCode() : 0);
        result = 31 * result + (isContinuous ? 1 : 0);
        result = 31 * result + (start != null ? start.hashCode() : 0);
        result = 31 * result + (firstEvent != null ? firstEvent.hashCode() : 0);
        result = 31 * result + (lastEvent != null ? lastEvent.hashCode() : 0);
        result = 31 * result + (jvmHeaders != null ? jvmHeaders.hashCode() : 0);
        result = 31 * result + (jvmIds != null ? jvmIds.hashCode() : 0);
        result = 31 * result + (jvmNames != null ? jvmNames.hashCode() : 0);
        result = 31 * result + (jvmVersions != null ? jvmVersions.hashCode() : 0);
        result = 31 * result + (jvmGCTypes != null ? jvmGCTypes.hashCode() : 0);
        result = 31 * result + (jvmMemoryDetails != null ? jvmMemoryDetails.hashCode() : 0);
        result = 31 * result + (sourceType != null ? sourceType.hashCode() : 0);
        result = 31 * result + (sourceConfig != null ? sourceConfig.hashCode() : 0);
        result = 31 * result + (sourceByJvm != null ? sourceByJvm.hashCode() : 0);
        result = 31 * result + (sourceConfigByJvm != null ? sourceConfigByJvm.hashCode() : 0);
        result = 31 * result + (ext != null ? ext.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("GCAnalyseImpl{");
        sb.append("continuous=").append(isContinuous());
        sb.append(", id='").append(id).append('\'');
        sb.append(", accountId=").append(accountId);
        sb.append(", name='").append(name).append('\'');
        sb.append(", timezone='").append(timezone).append('\'');
        sb.append(", isContinuous=").append(isContinuous);
        sb.append(", start=").append(start);
        sb.append(", firstEvent=").append(firstEvent);
        sb.append(", lastEvent=").append(lastEvent);
        sb.append(", jvmHeaders=").append(jvmHeaders);
        sb.append(", jvmIds=").append(jvmIds);
        sb.append(", jvmNames=").append(jvmNames);
        sb.append(", jvmVersions=").append(jvmVersions);
        sb.append(", jvmGCTypes=").append(jvmGCTypes);
        sb.append(", jvmMemoryDetails=").append(jvmMemoryDetails);
        sb.append(", sourceType=").append(sourceType);
        sb.append(", sourceConfig='").append(sourceConfig).append('\'');
        sb.append(", sourceByJvm=").append(sourceByJvm);
        sb.append(", sourceConfigByJvm=").append(sourceConfigByJvm);
        sb.append(", ext='").append(ext).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
