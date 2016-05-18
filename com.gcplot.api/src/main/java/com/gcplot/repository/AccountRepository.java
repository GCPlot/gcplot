package com.gcplot.repository;

import com.gcplot.Identifier;
import com.gcplot.model.account.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {

    List<Account> accounts();

    Optional<Account> account(Identifier id);

    Optional<Account> account(String token);

    Optional<Account> account(String username, String passHash, LoginType type);

    Account store(Account account);

    void delete(Account account);

    boolean generateNewToken(String oldToken, String newToken);

    boolean confirm(String token, String salt);

    void block(String username);

    void unblock(String username);

    enum LoginType {
        USERNAME, EMAIL
    }

}
