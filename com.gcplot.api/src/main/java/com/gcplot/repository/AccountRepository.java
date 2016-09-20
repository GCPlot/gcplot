package com.gcplot.repository;

import com.gcplot.Identifier;
import com.gcplot.model.account.Account;
import com.gcplot.model.role.Role;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {

    List<Account> accounts();

    Optional<Account> account(Identifier id);

    Optional<Account> account(String token);

    Optional<Account> account(String username, String passHash, LoginType type);

    Account insert(Account account);

    Account updateInfo(Account account);

    void delete(Account account);

    boolean generateNewToken(String oldToken, String newToken);

    boolean changePassword(Account account, String newPasshash);

    boolean confirm(String token, String salt);

    void block(String username);

    void unblock(String username);

    void attachRole(Account account, Role role);

    void detachRole(Account account, Role role);

    void roleManagement(Identifier account, boolean isRoleManagement);

    enum LoginType {
        USERNAME, EMAIL
    }

}
