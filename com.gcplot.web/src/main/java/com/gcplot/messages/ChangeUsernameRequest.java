package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         12/3/16
 */
public class ChangeUsernameRequest {
    @JsonProperty("new_username")
    public String username;

    public ChangeUsernameRequest(@JsonProperty("new_username") String username) {
        this.username = username;
    }
}
