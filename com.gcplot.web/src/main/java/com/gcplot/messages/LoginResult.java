package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.accounts.Account;

public class LoginResult {

    @JsonProperty("username")
    public String username;
    @JsonProperty("first_name")
    public String firstName;
    @JsonProperty("last_name")
    public String lastName;
    @JsonProperty("email")
    public String email;
    @JsonProperty("token")
    public String token;
    @JsonProperty("confirmed")
    public boolean confirmed;

    public LoginResult() {
    }

    public static LoginResult from(Account account) {
        LoginResult loginResult = new LoginResult();
        loginResult.username = account.username();
        loginResult.firstName = account.firstName();
        loginResult.lastName = account.lastName();
        loginResult.email = account.email();
        loginResult.token = account.token();
        loginResult.confirmed = account.isConfirmed();
        return loginResult;
    }

}
