package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         9/20/16
 */
public class PutConfigMessage {
    @JsonProperty(value = "name", required = true)
    public String propertyName;
    @JsonProperty(value = "value", required = true)
    public String value;
    @JsonProperty(value = "type", required = false, defaultValue = "string")
    public String type;

    public PutConfigMessage(@JsonProperty("name") String propertyName,
                            @JsonProperty("value") String value,
                            @JsonProperty(value = "type", required = false, defaultValue = "string") String type) {
        this.propertyName = propertyName;
        this.value = value;
        this.type = type;
    }
}
