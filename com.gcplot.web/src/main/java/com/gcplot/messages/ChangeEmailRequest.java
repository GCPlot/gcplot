package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         5/3/17
 */
public class ChangeEmailRequest {
    @JsonProperty(value = "new_email", required = true)
    public String newEmail;

    public ChangeEmailRequest(@JsonProperty(value = "new_email", required = true) String newEmail) {
        this.newEmail = newEmail;
    }

}
