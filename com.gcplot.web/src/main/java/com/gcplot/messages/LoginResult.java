package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.model.account.Account;

public class LoginResult {
    @JsonProperty("username")
    public String username;
    @JsonProperty("first_name")
    public String firstName;
    @JsonProperty("last_name")
    public String lastName;
    @JsonProperty("email")
    public String email;
    @JsonProperty("notification_email")
    public String notificationEmail;
    @JsonProperty("token")
    public String token;
    @JsonProperty("confirmed")
    public boolean confirmed;
    @JsonProperty("config")
    public AccountConfigResponse config;

    public LoginResult() {
    }

    public static LoginResult from(Account account) {
        LoginResult loginResult = new LoginResult();
        loginResult.username = account.username();
        loginResult.firstName = account.firstName();
        loginResult.lastName = account.lastName();
        loginResult.notificationEmail = account.notificationEmail();
        loginResult.email = account.email();
        loginResult.token = account.token();
        loginResult.confirmed = account.isConfirmed();
        loginResult.config = AccountConfigResponse.from(account.config());
        return loginResult;
    }

}
