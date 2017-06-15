package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.model.VMVersion;
import com.gcplot.model.gc.GCAnalyse;
import com.gcplot.model.gc.GarbageCollectorType;
import com.google.common.base.Strings;
import org.joda.time.DateTimeZone;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.gcplot.utils.CollectionUtils.transformValue;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/22/16
 */
public class AnalyseResponse {
    @JsonProperty("id")
    public String id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("tz")
    public String timezone;
    @JsonProperty("cnts")
    public boolean continuous;
    @JsonProperty("start_utc")
    public long startUTC;
    @JsonProperty("first_utc")
    public Map<String, Long> firstEventUTC;
    @JsonProperty("last_utc")
    public Map<String, Long> lastEventUTC;
    @JsonProperty("source_type")
    public String sourceType;
    @JsonProperty("source_config")
    public String sourceConfig;
    @JsonProperty("jvm_hdrs")
    public Map<String, String> jvmHeaders;
    @JsonProperty("jvm_vers")
    public Map<String, Integer> jvmVersions;
    @JsonProperty("jvm_gcts")
    public Map<String, Integer> jvmGCTypes;
    @JsonProperty("jvm_ids")
    public Set<String> jvmIds;
    @JsonProperty("jvm_names")
    public Map<String, String> jvmNames;
    @JsonProperty("jvm_mem")
    public Map<String, MemoryStatus> memory;
    @JsonProperty("ext")
    public String ext;

    public AnalyseResponse(@JsonProperty("id") String id,
                           @JsonProperty("name") String name,
                           @JsonProperty("tz") String timezone,
                           @JsonProperty("cnts") boolean continuous,
                           @JsonProperty("start_utc") long startUTC,
                           @JsonProperty("first_utc") Map<String, Long> firstEventUTC,
                           @JsonProperty("last_utc") Map<String, Long> lastEventUTC,
                           @JsonProperty("source_type") String sourceType,
                           @JsonProperty("source_config") String sourceConfig,
                           @JsonProperty("jvm_hdrs") Map<String, String> jvmHeaders,
                           @JsonProperty("jvm_vers") Map<String, Integer> jvmVersions,
                           @JsonProperty("jvm_gcts") Map<String, Integer> jvmGCTypes,
                           @JsonProperty("jvm_ids") Set<String> jvmIds,
                           @JsonProperty("jvm_names") Map<String, String> jvmNames,
                           @JsonProperty("jvm_mem") Map<String, MemoryStatus> memory,
                           @JsonProperty("ext") String ext) {
        this.id = id;
        this.name = name;
        this.timezone = timezone;
        this.continuous = continuous;
        this.startUTC = startUTC;
        this.firstEventUTC = firstEventUTC;
        this.lastEventUTC = lastEventUTC;
        this.sourceType = sourceType;
        this.sourceConfig = sourceConfig;
        this.jvmHeaders = jvmHeaders;
        this.jvmVersions = jvmVersions;
        this.jvmGCTypes = jvmGCTypes;
        this.jvmIds = jvmIds;
        this.jvmNames = jvmNames;
        this.memory = memory;
        this.ext = ext;
    }

    public AnalyseResponse(GCAnalyse analyse) {
        this.id = analyse.id();
        this.name = analyse.name();
        this.timezone = analyse.timezone();
        this.continuous = analyse.isContinuous();
        this.startUTC = analyse.start().toDateTime(DateTimeZone.UTC).getMillis();
        this.sourceType = analyse.sourceType().toString();
        this.sourceConfig = Strings.nullToEmpty(analyse.sourceConfig());
        this.firstEventUTC = analyse.firstEvent() != null ? transformValue(analyse.firstEvent(),
                v -> v.toDateTime(DateTimeZone.UTC).getMillis()) : Collections.emptyMap();
        this.lastEventUTC = analyse.lastEvent() != null ? transformValue(analyse.lastEvent(),
                v -> v.toDateTime(DateTimeZone.UTC).getMillis()) : Collections.emptyMap();
        this.jvmVersions = analyse.jvmVersions() != null ?
                transformValue(analyse.jvmVersions(), VMVersion::type) : Collections.emptyMap();
        this.jvmGCTypes = analyse.jvmVersions() != null ?
                transformValue(analyse.jvmGCTypes(), GarbageCollectorType::type) : Collections.emptyMap();
        this.jvmHeaders = analyse.jvmHeaders();
        this.jvmIds = analyse.jvmIds();
        this.jvmNames = analyse.jvmNames();
        this.memory = analyse.jvmMemoryDetails() == null ? Collections.emptyMap() : transformValue(analyse.jvmMemoryDetails(), MemoryStatus::new);
        this.ext = analyse.ext();
    }

}
