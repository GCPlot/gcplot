package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateAnalyseRequest {
    @JsonProperty(value = "id", required = true)
    public String id;
    @JsonProperty(value = "name", required = true)
    public String name;
    @JsonProperty(value = "timezone")
    public String timezone;
    @JsonProperty("ext")
    public String ext;

    public UpdateAnalyseRequest(@JsonProperty(value = "id", required = true) String id,
                                @JsonProperty(value = "name", required = true) String name,
                                @JsonProperty(value = "timezone") String timezone,
                                @JsonProperty("ext") String ext) {
        this.id = id;
        this.name = name;
        this.timezone = timezone;
        this.ext = ext;
    }
}
