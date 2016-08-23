package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/21/16
 */
public class NewAnalyseRequest {
    public String name;
    public boolean isContinuous;
    public int vmVersion;
    public int gcCollectorType;
    public Set<String> jvmIds;
    public String ext;

    public NewAnalyseRequest(@JsonProperty(value = "name", required = true) String name,
                             @JsonProperty(value = "continuous", required = true) boolean isContinuous,
                             @JsonProperty(value = "vm_version", required = true) int vmVersion,
                             @JsonProperty(value = "gc_collector_type", required = true) int gcCollectorType,
                             @JsonProperty(value = "jvm_ids", required = true) Set<String> jvmIds,
                             @JsonProperty(value = "jvm_ids", required = true) String ext) {
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
