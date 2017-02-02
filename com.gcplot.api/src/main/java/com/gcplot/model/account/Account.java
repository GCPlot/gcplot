package com.gcplot.model.account;

import com.gcplot.Identifier;
import com.gcplot.model.role.Role;
import org.joda.time.DateTime;

import java.util.List;

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

    boolean isRoleManagement();

    String confirmationSalt();

    List<Role> roles();

    DateTime registrationTime();
}
