package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterRequest {
    public String username;
    public String password;
    public String email;

    @JsonCreator
    public RegisterRequest(@JsonProperty(value = "username", required = true) String username,
                           @JsonProperty(value = "password", required = true) String password,
                           @JsonProperty(value = "email", required = true) String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public RegisterRequest() {
    }
}
