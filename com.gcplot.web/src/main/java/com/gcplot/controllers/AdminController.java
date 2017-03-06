package com.gcplot.controllers;

import com.gcplot.Identifier;
import com.gcplot.commons.ConfigProperty;
import com.gcplot.commons.ErrorMessages;
import com.gcplot.messages.AccountResponse;
import com.gcplot.messages.ConnectorSettingsMessage;
import com.gcplot.messages.PutConfigMessage;
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
    public static final String MESSAGE = "You are not allowed to perform such an action.";
    @Autowired
    private AccountRepository accountRepository;
    private String connectorS3Base;
    private String connectorS3Bucket;
    private String connectorS3AccessKey;
    private String connectorS3SecretKey;

    @PostConstruct
    public void init() {
        dispatcher.requireAuth()
                .filter(this::hasNonRestrictiveRole, MESSAGE)
                .filter(c -> c.hasParam("role_manager"), "Param 'role_manager' is missing.")
                .filter(c -> c.hasParam("account_id"), "Param 'account_id' is missing.")
                .get("/admin/account/roles_management", this::userRoleManagement);
        dispatcher.requireAuth()
                .filter(this::hasNonRestrictiveRole, MESSAGE)
                .filter(c -> c.hasParam("account_id"), "Param 'account_id' is missing.")
                .get("/admin/account/info", this::getAccountInfo);
        dispatcher.requireAuth()
                .get("/admin/account/id", this::getCurrentAccountId);
        dispatcher.requireAuth()
                .get("/connector/settings", this::getConnectorSettings);
        dispatcher.requireAuth()
                .filter(this::hasNonRestrictiveRole, MESSAGE)
                .post("/admin/configs/put", PutConfigMessage.class, this::putConfig);
    }

    /**
     * POST /admin/config/set
     * Require Auth (token)
     */
    public void putConfig(PutConfigMessage req, RequestContext ctx) {
        Object value = convertPropertyValue(req.value, req.type);
        if (value == null) {
            ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM,
                    String.format("Type '%s' wasn't found.", req.type)));
        } else {
            ConfigProperty cf = ConfigProperty.get(req.propertyName);
            if (cf == null) {
                ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM,
                        String.format("Property '%s' wasn't found.", req.propertyName)));
            } else {
                config.putProperty(cf, value);
                ctx.response(SUCCESS);
            }
        }
    }

    /**
     * GET /admin/account/id
     * Require Auth (token)
     */
    public void getCurrentAccountId(RequestContext ctx) {
        ctx.response(ctx.loginInfo().get().getAccount().id().toString());
    }

    /**
     * GET /connector/settings
     * Require Auth (token)
     */
    public void getConnectorSettings(RequestContext ctx) {
        ctx.response(new ConnectorSettingsMessage(connectorS3Bucket, connectorS3Base, connectorS3AccessKey, connectorS3SecretKey));
    }

    /**
     * GET /admin/account/info
     * Require Auth (token)
     * Require >= 1 non-restrictive roles
     * Params:
     *  - account_id (string)
     * Returns: Account in JSON except pass hash
     */
    public void getAccountInfo(RequestContext ctx) {
        String accountId = ctx.param("account_id");

        Optional<Account> account = accountRepository.account(Identifier.fromStr(accountId));
        if (account.isPresent()) {
            ctx.response(AccountResponse.from(account.get()));
        } else {
            ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM,
                    String.format("Account with [id=%s] wasn't found", accountId)));
        }
    }

    /**
     * GET /admin/account/roles_management
     * Require Auth (token)
     * Require >= 1 non-restrictive roles
     * Params:
     *  - role_manager (boolean)
     *  - account_id (string)
     */
    public void userRoleManagement(RequestContext ctx) {
        boolean isRoleManager = Boolean.parseBoolean(ctx.param("role_manager"));
        String accountId = ctx.param("account_id");

        Optional<Account> account = accountRepository.account(Identifier.fromStr(accountId));
        if (account.isPresent()) {
            accountRepository.roleManagement(account.get().id(), isRoleManager);
            ctx.response(SUCCESS);
        } else {
            ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM,
                    String.format("Account with [id=%s] wasn't found", accountId)));
        }
    }

    private Object convertPropertyValue(String value, String type) {
        switch (type) {
            case "byte": return Byte.parseByte(value);
            case "short": return Short.parseShort(value);
            case "int": return Integer.parseInt(value);
            case "long": return Long.parseLong(value);
            case "boolean": return Boolean.parseBoolean(value);
            case "float": return Float.parseFloat(value);
            case "double": return Double.parseDouble(value);
            case "string": return value;
            default: return null;
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

    public void setConnectorS3Base(String connectorS3Base) {
        this.connectorS3Base = connectorS3Base;
    }

    public void setConnectorS3Bucket(String connectorS3Bucket) {
        this.connectorS3Bucket = connectorS3Bucket;
    }

    public void setConnectorS3AccessKey(String connectorS3AccessKey) {
        this.connectorS3AccessKey = connectorS3AccessKey;
    }

    public void setConnectorS3SecretKey(String connectorS3SecretKey) {
        this.connectorS3SecretKey = connectorS3SecretKey;
    }
}
