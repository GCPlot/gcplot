package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         10/26/16
 */
public class BulkJvmsUpdateRequest {
    @JsonProperty("an_id")
    public String analyseId;
    @JsonProperty("add_jvms")
    public List<AddJvmRequest> newJvms;
    @JsonProperty("remove_jvms")
    public List<String> removeJvms;
    @JsonProperty("update_jvms")
    public List<UpdateJvmVersionRequest> updateJvms;

    public BulkJvmsUpdateRequest(@JsonProperty("an_id") String analyseId,
                                 @JsonProperty("add_jvms") List<AddJvmRequest> newJvms,
                                 @JsonProperty("remove_jvms") List<String> removeJvms,
                                 @JsonProperty("update_jvms") List<UpdateJvmVersionRequest> updateJvms) {
        this.analyseId = analyseId;
        this.newJvms = newJvms;
        this.removeJvms = removeJvms;
        this.updateJvms = updateJvms;
    }
}
