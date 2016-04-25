package com.gcplot.model;

import com.gcplot.Identifier;

public interface Account {

    Identifier id();

    String username();

    String email();

    String token();

    String passHash();

    boolean isConfirmed();

    String confirmationSalt();
}
