package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         9/19/16
 */
public class AttachRoleMessage {
    @JsonProperty(value = "account_id", required = true)
    public String accountId;
    @JsonProperty(value = "role_id", required = true)
    public String roleId;

    public AttachRoleMessage(@JsonProperty(value = "account_id", required = true) String accountId,
                             @JsonProperty(value = "role_id", required = true) String roleId) {
        this.accountId = accountId;
        this.roleId = roleId;
    }
}
