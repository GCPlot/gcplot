package com.gcplot.model;

import com.gcplot.Identifier;

import java.util.List;
import java.util.Optional;

public interface AccountsRepository {

    List<Account> accounts();

    Optional<Account> account(Identifier id);

    Optional<Account> account(String token);

    Optional<Account> account(String username, String passHash);

    void store(Account account);

    Optional<Account> delete(Identifier identifier);

    String generateNewToken(String oldToken);

    boolean confirm(String token, String salt);

}
