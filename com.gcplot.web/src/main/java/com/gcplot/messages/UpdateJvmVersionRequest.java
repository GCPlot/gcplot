package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/30/16
 */
public class UpdateJvmVersionRequest {
    @JsonProperty(value = "an_id", required = true)
    public String analyseId;
    @JsonProperty(value = "jvm_id", required = true)
    public String jvmId;
    @JsonProperty(value = "name")
    public String jvmName;
    @JsonProperty("vm_ver")
    public Integer vmVersion;
    @JsonProperty("gc_type")
    public Integer gcType;

    public UpdateJvmVersionRequest(@JsonProperty(value = "an_id", required = true) String analyseId,
                                   @JsonProperty(value = "jvm_id", required = true) String jvmId,
                                   @JsonProperty(value = "name") String jvmName,
                                   @JsonProperty("vm_ver") Integer vmVersion,
                                   @JsonProperty("gc_type") Integer gcType) {
        this.analyseId = analyseId;
        this.jvmId = jvmId;
        this.jvmName = jvmName;
        this.vmVersion = vmVersion;
        this.gcType = gcType;
    }

}
