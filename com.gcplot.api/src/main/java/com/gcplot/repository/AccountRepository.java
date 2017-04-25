package com.gcplot.repository;

import com.gcplot.Identifier;
import com.gcplot.model.account.Account;
import com.gcplot.model.role.Role;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {

    List<Account> accounts();

    Optional<Identifier> map(String username);

    Optional<Account> account(Identifier id);

    Optional<Account> account(String token);

    Optional<Account> account(String username, String passHash, LoginType type);

    Optional<Account> find(String username, LoginType type);

    Account insert(Account account);

    Account updateInfo(Account account);

    void delete(Account account);

    boolean generateNewToken(String oldToken, String newToken);

    boolean changePassword(Account account, String newPasshash);

    boolean changeUsername(Account account, String newUsername);

    boolean confirm(String token, String salt);

    void block(String username);

    void unblock(String username);

    void attachNewIp(Account account, String ip);

    void attachRole(Account account, Role role);

    void detachRole(Account account, Role role);

    void roleManagement(Identifier account, boolean isRoleManagement);

    enum LoginType {
        USERNAME, EMAIL
    }

}
