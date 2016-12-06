package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         12/6/16
 */
public class SendNewPassRequest {
    @JsonProperty(value = "email", required = true)
    public String email;

    public SendNewPassRequest(@JsonProperty(value = "email", required = true) String email) {
        this.email = email;
    }

}
