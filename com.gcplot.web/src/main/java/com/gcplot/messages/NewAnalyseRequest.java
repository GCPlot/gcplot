package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/21/16
 */
public class NewAnalyseRequest {
    @JsonProperty(value = "name")
    public String name;
    @JsonProperty(value = "cnts")
    public boolean isContinuous;
    @JsonProperty(value = "ext")
    public String ext;

    public NewAnalyseRequest(@JsonProperty(value = "name", required = true) String name,
                             @JsonProperty(value = "cnts", required = true) boolean isContinuous,
                             @JsonProperty(value = "ext", required = true) String ext) {
        this.name = name;
        this.isContinuous = isContinuous;
        this.ext = ext;
    }

    public NewAnalyseRequest() {
    }
}
