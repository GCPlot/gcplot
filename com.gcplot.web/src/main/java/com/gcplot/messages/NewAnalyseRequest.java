package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/21/16
 */
public class NewAnalyseRequest {
    @JsonProperty(value = "name")
    public String name;
    @JsonProperty(value = "continuous")
    public boolean isContinuous;
    @JsonProperty(value = "vm_version")
    public int vmVersion;
    @JsonProperty(value = "gc_collector_type")
    public int gcCollectorType;
    @JsonProperty(value = "jvm_ids")
    public Set<String> jvmIds;
    @JsonProperty(value = "ext")
    public String ext;

    public NewAnalyseRequest(@JsonProperty(value = "name", required = true) String name,
                             @JsonProperty(value = "continuous", required = true) boolean isContinuous,
                             @JsonProperty(value = "vm_version", required = true) int vmVersion,
                             @JsonProperty(value = "gc_collector_type", required = true) int gcCollectorType,
                             @JsonProperty(value = "jvm_ids", required = true) Set<String> jvmIds,
                             @JsonProperty(value = "ext", required = true) String ext) {
        this.name = name;
        this.isContinuous = isContinuous;
        this.vmVersion = vmVersion;
        this.gcCollectorType = gcCollectorType;
        this.jvmIds = jvmIds;
        this.ext = ext;
    }

    public NewAnalyseRequest() {
    }
}
