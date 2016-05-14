package com.gcplot.accounts;

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
