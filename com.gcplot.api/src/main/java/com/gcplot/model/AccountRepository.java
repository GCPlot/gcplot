package com.gcplot.model;

import com.gcplot.Identifier;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {

    List<Account> accounts();

    Optional<Account> account(Identifier id);

    Optional<Account> account(String token);

    Optional<Account> account(String username, String passHash);

    Account store(Account account);

    void delete(Account account);

    boolean generateNewToken(String oldToken, String newToken);

    boolean confirm(String token, String salt);

}
