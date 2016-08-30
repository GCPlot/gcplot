package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/30/16
 */
public class UpdateJvmInfoRequest {

    @JsonProperty(value = "an_id", required = true)
    public String analyseId;
    @JsonProperty(value = "jvm_id", required = true)
    public String jvmId;
    @JsonProperty("headers")
    public String headers;
    @JsonProperty("mem")
    public MemoryStatus memoryStatus;

    public UpdateJvmInfoRequest(@JsonProperty(value = "an_id", required = true) String analyseId,
                                @JsonProperty(value = "jvm_id", required = true) String jvmId,
                                @JsonProperty("headers") String headers,
                                @JsonProperty("mem") MemoryStatus memoryStatus) {
        this.analyseId = analyseId;
        this.jvmId = jvmId;
        this.headers = headers;
        this.memoryStatus = memoryStatus;
    }

}
