package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gcplot.model.gc.SourceType;

import java.io.IOException;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/23/17
 */
public class UpdateAnalyzeSourceRequest {
    @JsonProperty(value = "id", required = true)
    public String id;
    @JsonProperty(value = "source_type", required = true)
    public SourceType sourceType;
    @JsonProperty(value = "source_config", required = true)
    public String sourceConfig;

    public UpdateAnalyzeSourceRequest(@JsonProperty(value = "id", required = true) String id,
                                      @JsonProperty(value = "source_type", required = true) SourceType sourceType,
                                      @JsonProperty(value = "source_config", required = true) String sourceConfig) {
        this.id = id;
        this.sourceType = sourceType;
        this.sourceConfig = sourceConfig;
    }
}
