package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/4/17
 */
public class UpdateAccountConfigRequest {
    @JsonProperty(value = "prop_id", required = true)
    public String propertyId;
    @JsonProperty(value = "value", required = true)
    public String value;

    public UpdateAccountConfigRequest(@JsonProperty(value = "prop_id", required = true) String propertyId,
                                      @JsonProperty(value = "value", required = true) String value) {
        this.propertyId = propertyId;
        this.value = value;
    }

}
