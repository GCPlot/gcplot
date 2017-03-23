package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.model.gc.SourceType;

import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/21/16
 */
public class NewAnalyseRequest {
    @JsonProperty(value = "name")
    public String name;
    @JsonProperty(value = "cnts")
    public boolean isContinuous;
    @JsonProperty(value = "tz")
    public String timezone;
    @JsonProperty(value = "jvms")
    public List<AddJvmRequest> jvms;
    @JsonProperty(value = "source_type")
    public SourceType sourceType;
    @JsonProperty(value = "source_config")
    public String sourceConfig;
    @JsonProperty(value = "ext")
    public String ext;

    public NewAnalyseRequest(@JsonProperty(value = "name", required = true) String name,
                             @JsonProperty(value = "cnts", required = true) boolean isContinuous,
                             @JsonProperty(value = "tz") String timezone,
                             @JsonProperty(value = "jvms") List<AddJvmRequest> jvms,
                             @JsonProperty(value = "source_type") SourceType sourceType,
                             @JsonProperty(value = "source_config") String sourceConfig,
                             @JsonProperty(value = "ext", required = true) String ext) {
        this.name = name;
        this.isContinuous = isContinuous;
        this.timezone = timezone;
        this.jvms = jvms;
        this.sourceType = sourceType;
        this.sourceConfig = sourceConfig;
        this.ext = ext;
    }

    public NewAnalyseRequest() {
    }
}
