package com.gcplot.web;

import com.gcplot.model.account.Account;

public class LoginInfo {

    private final String token;
    public String token() {
        return token;
    }

    private final Account account;
    public Account getAccount() {
        return account;
    }

    public LoginInfo(String token, Account account) {
        this.token = token;
        this.account = account;
    }

}
