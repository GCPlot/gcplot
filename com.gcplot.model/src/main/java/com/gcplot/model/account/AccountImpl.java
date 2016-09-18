package com.gcplot.model.account;

import com.gcplot.Identifier;
import com.gcplot.model.role.Role;
import com.gcplot.model.role.RoleImpl;
import com.google.common.base.MoreObjects;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    public boolean isRoleManagement() {
        return roleManagement;
    }
    public void setRoleManagement(boolean roleManagement) {
        this.roleManagement = roleManagement;
    }

    @Override
    public String confirmationSalt() {
        return confirmationSalt;
    }
    public void setConfirmationSalt(String confirmationSalt) {
        this.confirmationSalt = confirmationSalt;
    }

    @Override
    public List<Role> roles() {
        return (List<Role>) roles;
    }
    public List<RoleImpl> rolesImpl() {
        return (List<RoleImpl>) roles;
    }
    public void addRole(RoleImpl role) {
        roles.add(role);
    }
    public void removeRole(RoleImpl role) {
        Iterator<RoleImpl> i = (Iterator<RoleImpl>) roles.iterator();
        while (i.hasNext()) {
            if (i.next().id().equals(role.id())) {
                i.remove();
            }
        }
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
                          String passHash, boolean confirmed, String confirmationSalt,
                          ArrayList<RoleImpl> roles) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.token = token;
        this.passHash = passHash;
        this.confirmed = confirmed;
        this.confirmationSalt = confirmationSalt;
        this.roles = roles;
    }

    public static AccountImpl createNew(String username,
                                        String firstName, String lastName,
                                        String email, String token, String passHash,
                                        String confirmationSalt, ArrayList<RoleImpl> roles) {
        return new AccountImpl(username, firstName, lastName,
                email, token, passHash, false, confirmationSalt, roles);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountImpl account = (AccountImpl) o;

        if (confirmed != account.confirmed) return false;
        if (blocked != account.blocked) return false;
        if (identifier != null ? !identifier.equals(account.identifier) : account.identifier != null) return false;
        if (username != null ? !username.equals(account.username) : account.username != null) return false;
        if (email != null ? !email.equals(account.email) : account.email != null) return false;
        if (firstName != null ? !firstName.equals(account.firstName) : account.firstName != null) return false;
        if (lastName != null ? !lastName.equals(account.lastName) : account.lastName != null) return false;
        if (token != null ? !token.equals(account.token) : account.token != null) return false;
        if (passHash != null ? !passHash.equals(account.passHash) : account.passHash != null) return false;
        if (confirmationSalt != null ? !confirmationSalt.equals(account.confirmationSalt) : account.confirmationSalt != null)
            return false;
        return roles != null ? roles.equals(account.roles) : account.roles == null;
    }

    @Override
    public int hashCode() {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (passHash != null ? passHash.hashCode() : 0);
        result = 31 * result + (confirmed ? 1 : 0);
        result = 31 * result + (blocked ? 1 : 0);
        result = 31 * result + (confirmationSalt != null ? confirmationSalt.hashCode() : 0);
        result = 31 * result + (roles != null ? roles.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("identifier", identifier)
                .add("username", username)
                .add("email", email)
                .add("firstName", firstName)
                .add("lastName", lastName)
                .add("token", token)
                .add("passHash", passHash)
                .add("confirmed", confirmed)
                .add("blocked", blocked)
                .add("confirmationSalt", confirmationSalt)
                .add("roles", roles)
                .add("version", version)
                .toString();
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
    protected boolean roleManagement;
    @OneToMany(targetEntity = RoleImpl.class)
    protected ArrayList<? super RoleImpl> roles;
}
