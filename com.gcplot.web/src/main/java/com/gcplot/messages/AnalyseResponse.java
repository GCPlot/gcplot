package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.model.VMVersion;
import com.gcplot.model.gc.GCAnalyse;
import com.gcplot.model.gc.GarbageCollectorType;
import com.gcplot.model.gc.MemoryDetails;
import org.joda.time.DateTimeZone;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.gcplot.commons.CollectionUtils.*;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/22/16
 */
public class AnalyseResponse {
    @JsonProperty(value = "id")
    public String id;
    @JsonProperty(value = "name")
    public String name;
    @JsonProperty(value = "cnts")
    public boolean continuous;
    @JsonProperty(value = "start_utc")
    public long startUTC;
    @JsonProperty(value = "last_utc")
    public long lastEventUTC;
    @JsonProperty(value = "jvm_hdrs")
    public Map<String, String> jvmHeaders;
    @JsonProperty(value = "jvm_vers")
    public Map<String, Integer> jvmVersions;
    @JsonProperty(value = "jvm_gcts")
    public Map<String, Integer> jvmGCTypes;
    @JsonProperty(value = "jvm_ids")
    public Set<String> jvmIds;
    @JsonProperty(value = "jvm_mem")
    public Map<String, Memory> memory;
    @JsonProperty(value = "ext")
    public String ext;

    public AnalyseResponse(GCAnalyse analyse) {
        this.id = analyse.id();
        this.name = analyse.name();
        this.continuous = analyse.isContinuous();
        this.startUTC = analyse.start().toDateTime(DateTimeZone.UTC).getMillis();
        this.lastEventUTC = analyse.lastEvent() != null ? analyse.lastEvent().toDateTime(DateTimeZone.UTC).getMillis() : 0;
        this.jvmVersions = analyse.jvmVersions() != null ?
                transformValue(analyse.jvmVersions(), VMVersion::type) : Collections.emptyMap();
        this.jvmGCTypes = analyse.jvmVersions() != null ?
                transformValue(analyse.jvmGCTypes(), GarbageCollectorType::type) : Collections.emptyMap();
        this.jvmHeaders = analyse.jvmHeaders();
        this.jvmIds = analyse.jvmIds();
        this.memory = analyse.jvmMemoryDetails() == null ? Collections.emptyMap() : transformValue(analyse.jvmMemoryDetails(), Memory::new);
        this.ext = analyse.ext();
    }

    protected static class Memory {
        @JsonProperty(value = "ps")
        public long pageSize;
        @JsonProperty(value = "pt")
        public long physicalTotal;
        @JsonProperty(value = "pf")
        public long physicalFree;
        @JsonProperty(value = "st")
        public long swapTotal;
        @JsonProperty(value = "sf")
        public long swapFree;

        public Memory(MemoryDetails details) {
            this.pageSize = details.pageSize();
            this.physicalTotal = details.physicalTotal();
            this.physicalFree = details.physicalFree();
            this.swapTotal = details.swapTotal();
            this.swapFree = details.swapFree();
        }
    }

}
