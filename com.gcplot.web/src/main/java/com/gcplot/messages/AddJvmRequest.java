package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/28/16
 */
public class AddJvmRequest {
    @JsonProperty(value = "id", required = true)
    public String jvmId;
    @JsonProperty(value = "an_id", required = true)
    public String analyseId;
    @JsonProperty(value = "name", required = true)
    public String jvmName;
    @JsonProperty(value = "vm_ver", required = true)
    public int vmVersion;
    @JsonProperty(value = "gc_type", required = true)
    public int gcType;
    @JsonProperty("headers")
    public String headers;
    @JsonProperty("mem")
    public MemoryStatus memoryStatus;

    public AddJvmRequest(@JsonProperty(value = "id", required = true) String jvmId,
                         @JsonProperty(value = "an_id", required = true) String analyseId,
                         @JsonProperty(value = "name", required = true) String jvmName,
                         @JsonProperty(value = "vm_ver", required = true) int vmVersion,
                         @JsonProperty(value = "gc_type", required = true) int gcType,
                         @JsonProperty("headers") String headers,
                         @JsonProperty("mem") MemoryStatus memoryStatus) {
        this.jvmId = jvmId;
        this.analyseId = analyseId;
        this.jvmName = jvmName;
        this.vmVersion = vmVersion;
        this.gcType = gcType;
        this.headers = headers;
        this.memoryStatus = memoryStatus;
    }
}
