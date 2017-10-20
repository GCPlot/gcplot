package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.model.account.Account;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Only for Admin usage!
 *
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         9/18/16
 */
public class AccountResponse {
    @JsonProperty("id")
    public String id;
    @JsonProperty("username")
    public String username;
    @JsonProperty("email")
    public String email;
    @JsonProperty("notification_email")
    public String notificationEmail;
    @JsonProperty("first_name")
    public String firstName;
    @JsonProperty("last_name")
    public String lastName;
    @JsonProperty("token")
    public String token;
    @JsonProperty("confirmed")
    public boolean confirmed;
    @JsonProperty("blocked")
    public boolean blocked;
    @JsonProperty("role_mng")
    public boolean roleManagement;
    @JsonProperty("roles")
    public List<RoleResponse> roles;

    public AccountResponse(@JsonProperty("id") String id,
                           @JsonProperty("username") String username,
                           @JsonProperty("email") String email,
                           @JsonProperty("notification_email") String notificationEmail,
                           @JsonProperty("first_name") String firstName,
                           @JsonProperty("last_name") String lastName,
                           @JsonProperty("token") String token,
                           @JsonProperty("confirmed") boolean confirmed,
                           @JsonProperty("blocked") boolean blocked,
                           @JsonProperty("role_mng") boolean roleManagement,
                           @JsonProperty("roles") List<RoleResponse> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.notificationEmail = notificationEmail;
        this.firstName = firstName;
        this.lastName = lastName;
        this.token = token;
        this.confirmed = confirmed;
        this.blocked = blocked;
        this.roleManagement = roleManagement;
        this.roles = roles;
    }

    public static AccountResponse from(Account account) {
        List<RoleResponse> roles = account.roles().stream().map(RoleResponse::from).collect(Collectors.toList());
        return new AccountResponse(account.id().toString(), account.username(), account.email(), account.notificationEmail(),
                account.firstName(), account.lastName(), account.token(), account.isConfirmed(),
                account.isBlocked(), account.isRoleManagement(), roles);
    }

}
