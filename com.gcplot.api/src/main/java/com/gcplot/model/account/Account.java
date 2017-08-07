package com.gcplot.model.account;

import com.gcplot.Identifier;
import com.gcplot.model.account.config.Configuration;
import com.gcplot.model.role.Role;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Set;

public interface Account {

    Identifier id();

    String username();

    String email();

    String notificationEmail();

    String firstName();

    String lastName();

    String token();

    String passHash();

    Set<String> ips();

    boolean isConfirmed();

    boolean isBlocked();

    boolean isRoleManagement();

    String confirmationSalt();

    List<Role> roles();

    DateTime registrationTime();

    Configuration config();
}
