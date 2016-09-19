package com.gcplot.controllers;

import com.gcplot.Identifier;
import com.gcplot.commons.ErrorMessages;
import com.gcplot.messages.NewOrUpdateRoleRequest;
import com.gcplot.messages.RoleResponse;
import com.gcplot.model.role.Role;
import com.gcplot.model.role.RoleImpl;
import com.gcplot.repository.RolesRepository;
import com.gcplot.web.RequestContext;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
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
    }

    /**
     * POST /roles/store
     * Require Auth (token)
     * Accepts: NewOrUpdateRoleRequest JSON
     */
    public void storeRole(NewOrUpdateRoleRequest req, RequestContext ctx) {
        if (Strings.isNullOrEmpty(req.id)) {

        } else {
            Optional<Role> role = rolesRepository.role(Identifier.fromStr(req.id));
            if (role.isPresent()) {
                RoleImpl r = (RoleImpl) role.get();

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

    private String message() {
        return "Current Account isn't allowed for Role management.";
    }

    private Predicate<RequestContext> roleManagementOnly() {
        return c -> c.loginInfo().get().getAccount().isRoleManagement();
    }

    @Autowired
    protected RolesRepository rolesRepository;

}
