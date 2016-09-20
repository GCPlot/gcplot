package com.gcplot.controllers;

import com.gcplot.Identifier;
import com.gcplot.commons.ErrorMessages;
import com.gcplot.messages.AttachRoleMessage;
import com.gcplot.messages.NewOrUpdateRoleRequest;
import com.gcplot.messages.RoleResponse;
import com.gcplot.model.account.Account;
import com.gcplot.model.role.Role;
import com.gcplot.model.role.RoleImpl;
import com.gcplot.repository.AccountRepository;
import com.gcplot.repository.RolesRepository;
import com.gcplot.web.RequestContext;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/31/16
 */
public class RolesController extends Controller {

    @PostConstruct
    public void init() {
        dispatcher.requireAuth().filter(roleManagementOnly(), message())
                .get("/roles/all", this::getAllRoles);
        dispatcher.requireAuth().filter(roleManagementOnly(), message())
                .get("/roles/default", this::getDefaultRoles);
        dispatcher.requireAuth().filter(roleManagementOnly(), message())
                .post("/roles/store", NewOrUpdateRoleRequest.class, this::storeRole);
        dispatcher.requireAuth().filter(roleManagementOnly(), message())
                .post("/roles/account/attach", AttachRoleMessage.class, this::attachRole);
        dispatcher.requireAuth().filter(roleManagementOnly(), message())
                .post("/roles/account/detach", AttachRoleMessage.class, this::detachRole);
    }

    /**
     * POST /roles/account/attach
     * Require Auth (token)
     * Accepts: AttachRoleMessage JSON
     */
    public void detachRole(AttachRoleMessage req, RequestContext ctx) {
        attachOrDetachRole(req, ctx, false);
    }

    /**
     * POST /roles/account/attach
     * Require Auth (token)
     * Accepts: AttachRoleMessage JSON
     */
    public void attachRole(AttachRoleMessage req, RequestContext ctx) {
        attachOrDetachRole(req, ctx, true);
    }

    private void attachOrDetachRole(AttachRoleMessage req, RequestContext ctx, boolean isAttach) {
        Optional<Account> account = accountRepository.account(Identifier.fromStr(req.accountId));
        Optional<Role> role = rolesRepository.role(Identifier.fromStr(req.roleId));

        if (role.isPresent() && account.isPresent()) {
            if (isAttach) {
                accountRepository.attachRole(account.get(), role.get());
            } else {
                accountRepository.detachRole(account.get(), role.get());
            }
            ctx.response(SUCCESS);
        } else {
            if (!account.isPresent()) {
                ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM,
                        String.format("Account [id=%s] wasn't found.", req.accountId)));
            } else {
                ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM,
                        String.format("Role [id=%s] wasn't found.", req.roleId)));
            }
        }
    }

    /**
     * POST /roles/store
     * Require Auth (token)
     * Accepts: NewOrUpdateRoleRequest JSON
     */
    public void storeRole(NewOrUpdateRoleRequest req, RequestContext ctx) {
        if (Strings.isNullOrEmpty(req.id)) {
            RoleImpl newRole = new RoleImpl(req.title);
            newRole.setDefault(req.isDefault);
            newRole.setRestrictions(mapRestrictions(req));
            String newId = rolesRepository.store(newRole).id().toString();
            ctx.response(newId);
        } else {
            Optional<Role> role = rolesRepository.role(Identifier.fromStr(req.id));
            if (role.isPresent()) {
                RoleImpl r = (RoleImpl) role.get();
                r.setRestrictions(mapRestrictions(req));
                r.setDefault(req.isDefault);
                r.setTitle(req.title);
                rolesRepository.store(r);
                ctx.response(SUCCESS);
            } else {
                ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM,
                        String.format("Role with [id=%s] wasn't found.", req.id)));
            }
        }
    }

    /**
     * GET /roles/default
     * Require Auth (token)
     */
    public void getDefaultRoles(RequestContext ctx) {
        ctx.response(rolesRepository.defaultRoles().stream().map(RoleResponse::from).collect(Collectors.toList()));
    }

    /**
     * GET /roles/all
     * Require Auth (token)
     */
    public void getAllRoles(RequestContext ctx) {
        ctx.response(rolesRepository.roles().stream().map(RoleResponse::from).collect(Collectors.toList()));
    }

    private List<RoleImpl.RestrictionImpl> mapRestrictions(NewOrUpdateRoleRequest req) {
        return req.restrictions.stream().map(r -> new RoleImpl.RestrictionImpl(r.type,
                r.restricted, r.action, r.amount, r.properties)).collect(Collectors.toList());
    }

    private String message() {
        return "Current Account isn't allowed for Role management.";
    }

    private Predicate<RequestContext> roleManagementOnly() {
        return c -> c.loginInfo().get().getAccount().isRoleManagement();
    }

    @Autowired
    protected RolesRepository rolesRepository;
    @Autowired
    protected AccountRepository accountRepository;
}
