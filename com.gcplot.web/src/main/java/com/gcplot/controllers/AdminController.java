package com.gcplot.controllers;

import com.gcplot.commons.ErrorMessages;
import com.gcplot.model.account.Account;
import com.gcplot.model.role.Restriction;
import com.gcplot.model.role.RestrictionType;
import com.gcplot.repository.AccountRepository;
import com.gcplot.web.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/21/16
 */
public class AdminController extends Controller {

    @PostConstruct
    public void init() {
        dispatcher.requireAuth()
                .filter(this::hasNonRestrictiveRole, "You are not allowed to perform such action.")
                .filter(c -> c.hasParam("role_manager"), "Param 'role_manager' is missing.")
                .filter(c -> c.hasParam("user_id"), "Param 'user_id' is missing.")
                .get("/user/management/roles", this::userRoleManagement);
    }

    /**
     * GET /user/management/roles
     * Require Auth (token)
     * Require >= 1 non-restrictive roles
     * Params:
     *  - role_manager (boolean)
     *  - user_id (string)
     */
    public void userRoleManagement(RequestContext ctx) {
        boolean isRoleManager = Boolean.parseBoolean(ctx.param("role_manager"));
        String userId = ctx.param("user_id");

        Optional<Account> account = accountRepository.account(userId);
        if (account.isPresent()) {
            accountRepository.roleManagement(account.get(), isRoleManager);
            ctx.response(SUCCESS);
        } else {
            ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM,
                    String.format("Account with [id=%s] wasn't found", userId)));
        }
    }

    private boolean hasNonRestrictiveRole(RequestContext ctx) {
        if (!ctx.loginInfo().isPresent()) {
            return false;
        }
        List<Restriction> rs = ctx.loginInfo().get().getAccount().roles().stream()
                .flatMap(r -> r.restrictions().stream())
                .filter(r -> r.type() == RestrictionType.TOGGLE)
                .filter(r -> r.action().equals(ctx.path())).collect(Collectors.toList());
        return rs.size() > 0 && rs.stream().allMatch(r -> !r.restricted());
    }

    protected AccountRepository accountRepository;
    public AccountRepository getAccountRepository() {
        return accountRepository;
    }
    @Autowired
    public void setAccountRepository(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

}
