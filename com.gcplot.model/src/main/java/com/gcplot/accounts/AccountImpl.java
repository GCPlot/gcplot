package com.gcplot.accounts;

import com.gcplot.Identifier;

import javax.persistence.*;

@Table(name = "Account", uniqueConstraints =
        @UniqueConstraint(columnNames={"username", "email", "token"}))
public class AccountImpl implements Account {

    @Override
    public Identifier id() {
        if (identifier == null) {
            identifier = Identifier.fromStr(id.toString());
        }
        return identifier;
    }
    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
        this.id = identifier.toString();
    }

    @Override
    public String username() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String email() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String firstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String lastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String token() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String passHash() {
        return passHash;
    }
    public void setPassHash(String passHash) {
        this.passHash = passHash;
    }

    @Override
    public boolean isConfirmed() {
        return confirmed;
    }
    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    @Override
    public boolean isBlocked() {
        return blocked;
    }
    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    @Override
    public String confirmationSalt() {
        return confirmationSalt;
    }
    public void setConfirmationSalt(String confirmationSalt) {
        this.confirmationSalt = confirmationSalt;
    }

    @Version
    private Object version;
    public Object getVersion() {
        return version;
    }
    public void setVersion(Object version) {
        this.version = version;
    }

    public Object getOId() {
        return id;
    }

    public AccountImpl() {
    }

    protected AccountImpl(String username, String firstName, String lastName,
                          String email, String token,
                          String passHash, boolean confirmed, String confirmationSalt) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.token = token;
        this.passHash = passHash;
        this.confirmed = confirmed;
        this.confirmationSalt = confirmationSalt;
    }

    public static AccountImpl createNew(String username,
                                        String firstName, String lastName,
                                        String email, String token, String passHash,
                                        String confirmationSalt) {
        return new AccountImpl(username, firstName, lastName,
                email, token, passHash, false, confirmationSalt);
    }

    @Id
    protected Object id;
    @Transient
    protected transient Identifier identifier;
    protected String username;
    protected String email;
    protected String firstName;
    protected String lastName;
    protected String token;
    protected String passHash;
    protected boolean confirmed;
    protected boolean blocked;
    protected String confirmationSalt;
}
