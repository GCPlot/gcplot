package com.gcplot.model.account;

import com.gcplot.Identifier;

public interface Account {

    Identifier id();

    String username();

    String email();

    String firstName();

    String lastName();

    String token();

    String passHash();

    boolean isConfirmed();

    boolean isBlocked();

    String confirmationSalt();
}
