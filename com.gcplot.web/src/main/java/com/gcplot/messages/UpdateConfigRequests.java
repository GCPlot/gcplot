package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class UpdateConfigRequests {
    @JsonProperty(value = "configs", required = true)
    public List<UpdateConfigRequest> configs;

    public UpdateConfigRequests(@JsonProperty(value = "configs", required = true)
                                        List<UpdateConfigRequest> configs) {
        this.configs = configs;
    }

}
