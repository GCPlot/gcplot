package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/15/16
 */
public class ChangePasswordRequest {
    @JsonProperty(value = "old_password")
    public String oldPassword;
    @JsonProperty(value = "salt")
    public String salt;
    @JsonProperty(value = "new_password", required = true)
    public String newPassword;

    public ChangePasswordRequest(@JsonProperty(value = "old_password") String oldPassword,
                                 @JsonProperty(value = "salt") String salt,
                                 @JsonProperty(value = "new_password", required = true) String newPassword) {
        this.oldPassword = oldPassword;
        this.salt = salt;
        this.newPassword = newPassword;
    }

    public ChangePasswordRequest() {
    }

}
