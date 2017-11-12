package com.gcplot.controllers;

import com.gcplot.configuration.ConfigProperty;
import com.gcplot.commons.ErrorMessages;
import com.gcplot.services.mail.MailService;
import com.gcplot.utils.Utils;
import com.gcplot.commons.exceptions.NotUniqueException;
import com.gcplot.messages.*;
import com.gcplot.model.account.Account;
import com.gcplot.model.account.AccountImpl;
import com.gcplot.repository.AccountRepository;
import com.gcplot.services.UrlBuilder;
import com.gcplot.web.RequestContext;
import com.google.common.base.Strings;
import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.regex.Pattern;

public class LoginController extends Controller {
    private AccountRepository accountRepository;
    private MailService mailService;
    private UrlBuilder urlBuilder;

    @PostConstruct
    public void init() {
        dispatcher.noAuth().filter(c -> c.hasParam("login") && c.hasParam("password"),
                "Username and password are required!").get("/user/login", this::login);
        dispatcher.noAuth().blocking().post("/user/register", RegisterRequest.class, this::register);
        dispatcher.requireAuth().allowNotConfirmed().get("/user/info", this::userInfo);
        dispatcher.requireAuth().allowNotConfirmed().filter(c -> c.hasParam("salt"),
                "Salt should be provided!").get("/user/confirm", this::confirm);
        dispatcher.requireAuth()
                .get("/user/account/id", this::getCurrentAccountId);
        dispatcher.requireAuth().allowNotConfirmed()
                .post("/user/change_password", ChangePasswordRequest.class, this::changePassword);
        dispatcher.requireAuth().allowNotConfirmed()
                .post("/user/change_username", ChangeUsernameRequest.class, this::changeUsername);
        dispatcher.requireAuth().allowNotConfirmed()
                .post("/user/change_email", ChangeEmailRequest.class, this::changeEmail);
        dispatcher.requireAuth()
                .post("/user/change_notification_email", ChangeEmailRequest.class, this::changeNotificationEmail);
        dispatcher.requireAuth().allowNotConfirmed()
                .post("/user/config/update", UpdateConfigRequest.class, this::updateConfig);
        dispatcher.noAuth().blocking().get("/user/register_admin", this::registerAdmin);
        dispatcher.allowNotConfirmed().requireAuth().get("/user/send/confirmation", this::sendConfirmation);
        dispatcher.noAuth().allowNotConfirmed().post("/user/send/new_password", SendNewPassRequest.class,
                this::sendNewPass);
    }

    /**
     * POST /user/config/update
     * Require Auth (token)
     * Payload: UpdateConfigRequest
     */
    private void updateConfig(UpdateConfigRequest req, RequestContext ctx) {
        com.gcplot.model.account.ConfigProperty cp = com.gcplot.model.account.ConfigProperty.by(req.propertyId);
        if (cp != null) {
            accountRepository.updateConfig(account(ctx), cp, req.value);
        }
        ctx.response(SUCCESS);
    }

    /**
     * GET /user/login
     * No Auth
     * Params:
     *   - login (String)
     *   - password (String)
     *
     * @param request
     */
    public void login(RequestContext request) {
        String login = request.param("login");
        String password = request.param("password");

        Optional<Account> r =
                getAccountRepository().account(login, hashPass(password), guess(login));
        if (r.isPresent()) {
            request.response(LoginResult.from(r.get()));
        } else {
            request.write(ErrorMessages.buildJson(ErrorMessages.WRONG_CREDENTIALS));
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
     * GET /user/info
     * Require Auth (token)
     * No params
     *
     * @param ctx
     */
    public void userInfo(RequestContext ctx) {
        ctx.response(LoginResult.from(account(ctx)));
    }

    /**
     * POST /user/register
     * No Auth
     * Body: RegisterRequest (JSON)
     *
     * @param request
     * @param c
     */
    public void register(RegisterRequest request, RequestContext c) {
        if (Strings.isNullOrEmpty(request.password)) {
            c.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM, "Password can't be empty."));
        } else if (request.password.length() < config.readInt(ConfigProperty.PASSWORD_MIN_LENGTH)) {
            c.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM, "Password must be longer than " +
                    config.readInt(ConfigProperty.PASSWORD_MIN_LENGTH) + " symbols."));
        } else {
            Account newAccount = AccountImpl.createNew(request.username, request.firstName, request.lastName,
                    request.email, DigestUtils.sha256Hex(Utils.getRandomIdentifier()),
                    hashPass(request.password), DigestUtils.sha256Hex(Utils.getRandomIdentifier()), new ArrayList<>(),
                    DateTime.now(DateTimeZone.UTC), new HashSet<>());
            try {
                getAccountRepository().insert(newAccount);
            } catch (NotUniqueException e) {
                c.write(ErrorMessages.buildJson(ErrorMessages.NOT_UNIQUE_FIELDS, "Username/E-mail must be unique."));
                return;
            }
            if (getMailService() != null) {
                try {
                    getMailService().sendConfirmationFor(newAccount);
                } catch (Throwable t) {
                    LOG.error(t.getMessage(), t);
                }
            }
            c.response(SUCCESS);
        }
    }

    /**
     * GET /user/confirm
     * Require Auth (token)
     * Params:
     *   - salt (String)
     *
     * @param ctx
     */
    public void confirm(RequestContext ctx) {
        if (getAccountRepository().confirm(token(ctx), ctx.param("salt"))) {
            ctx.redirect(urlBuilder.uiPostConfirmUrl());
        } else {
            ctx.write(ErrorMessages.buildJson(ErrorMessages.INTERNAL_ERROR,
                    String.format("Can't confirm user [username=%s]", account(ctx).username())));
        }
    }

    /**
     * POST /user/change_password
     * Require Auth (token)
     * Body: ChangePasswordRequest (JSON)
     *
     * @param c
     */
    public void changePassword(ChangePasswordRequest req, RequestContext c) {
        if (req.newPassword.equals(req.oldPassword)) {
            c.write(ErrorMessages.buildJson(ErrorMessages.SAME_PASSWORD));
        } else {
            if (!Strings.isNullOrEmpty(req.oldPassword) && !account(c).passHash().equals(hashPass(req.oldPassword))) {
                c.write(ErrorMessages.buildJson(ErrorMessages.OLD_PASSWORD_MISMATCH));
            } else if (!Strings.isNullOrEmpty(req.salt) && !account(c).confirmationSalt().equals(req.salt)) {
                c.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM, "Invalid salt."));
            } else if (Strings.isNullOrEmpty(req.oldPassword) && Strings.isNullOrEmpty(req.salt)) {
                c.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM,
                        "You must provide either salt or an old password."));
            } else if (req.newPassword.length() < config.readInt(ConfigProperty.PASSWORD_MIN_LENGTH)) {
                c.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM, "Password must be longer than " +
                        config.readInt(ConfigProperty.PASSWORD_MIN_LENGTH) + " symbols."));
            } else if (accountRepository.changePassword(account(c), hashPass(req.newPassword))) {
                c.response(SUCCESS);
            } else {
                c.write(ErrorMessages.buildJson(ErrorMessages.INTERNAL_ERROR,
                        String.format("Can't change password to user [username=%s]", account(c).username())));
            }
        }
    }

    /**
     * POST /user/change_username
     * Require Auth (token)
     * Body: ChangeUsernameRequest (JSON)
     *
     * @param c
     */
    public void changeUsername(ChangeUsernameRequest req, RequestContext c) {
        if (!Strings.isNullOrEmpty(req.username)) {
            if (accountRepository.changeUsername(account(c), req.username)) {
                c.response(SUCCESS);
            } else {
                c.write(ErrorMessages.buildJson(ErrorMessages.USER_ALREADY_EXISTS, "This username is already taken!"));
            }
        } else {
            c.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM, "Username can't be empty!"));
        }
    }

    /**
     * POST /user/send/new_password
     * Require Auth (token)
     * Body: SendNewPassRequest (JSON)
     */
    public void sendNewPass(SendNewPassRequest req, RequestContext c) {
        if (!Strings.isNullOrEmpty(req.email)) {
            if (EMAIL_PATTERN.matcher(req.email).matches()) {
                Optional<Account> ao = accountRepository.find(req.email, AccountRepository.LoginType.EMAIL);
                if (ao.isPresent()) {
                    mailService.sendNewPassUrl(ao.get());
                    c.response(SUCCESS);
                } else {
                    c.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM,
                            "User with email '" + req.email + "' not found."));
                }
            } else {
                c.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM, "Please enter a valid email."));
            }
        } else {
            c.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM, "Email can't be empty!"));
        }
    }

    /**
     * POST /user/change_email
     * Require Auth (token)
     * Body: ChangeEmailRequest (JSON)
     */
    private void changeEmail(ChangeEmailRequest req, RequestContext ctx) {
        Account account = account(ctx);
        if (Strings.isNullOrEmpty(req.newEmail) || !EMAIL_PATTERN.matcher(req.newEmail).matches()) {
            ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM, "Please enter a valid email."));
        } else {
            if (!accountRepository.changeEmail(account, req.newEmail)) {
                ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM, "Unable to change e-mail. " +
                        "Possibly it's already taken, please try again."));
            } else {
                if (!account.isConfirmed()) {
                    mailService.sendConfirmationFor(account);
                }
                ctx.response(SUCCESS);
            }
        }
    }

    /**
     * POST /user/change_notification_email
     * Require Auth (token)
     * Body: ChangeEmailRequest (JSON)
     */
    private void changeNotificationEmail(ChangeEmailRequest req, RequestContext ctx) {
        Account account = account(ctx);
        if (Strings.isNullOrEmpty(req.newEmail) || !EMAIL_PATTERN.matcher(req.newEmail).matches()) {
            ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM, "Please enter a valid notification email."));
        } else {
            if (!accountRepository.changeNotificationEmail(account, req.newEmail)) {
                ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM, "Unable to change notification e-mail. " +
                        "Possibly it's already taken, please try again."));
            } else {
                ctx.response(SUCCESS);
            }
        }
    }

    /**
     * GET /user/send/confirmation
     */
    public void sendConfirmation(RequestContext ctx) {
        Account account = account(ctx);
        mailService.sendConfirmationFor(account);
        ctx.response(SUCCESS);
    }

    private void registerAdmin(RequestContext ctx) {
        if (!config.readBoolean(ConfigProperty.IS_ADMIN_REGISTERED)) {
            config.putProperty(ConfigProperty.IS_ADMIN_REGISTERED, true);
            register(new RegisterRequest("admin", "", "", "admin", "admin@test.com"), ctx);
        } else {
            ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM, "You can register admin user only once."));
        }
    }

    private AccountRepository.LoginType guess(String username) {
        return EMAIL_PATTERN.matcher(username).matches() ? AccountRepository.LoginType.EMAIL
                : AccountRepository.LoginType.USERNAME;
    }

    private String hashPass(String p) {
        return DigestUtils.sha1Hex(DigestUtils.md5(p));
    }

    public AccountRepository getAccountRepository() {
        return accountRepository;
    }
    @Autowired
    public void setAccountRepository(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public MailService getMailService() {
        return mailService;
    }
    @Autowired
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    public UrlBuilder getUrlBuilder() {
        return urlBuilder;
    }
    @Autowired
    public void setUrlBuilder(UrlBuilder urlBuilder) {
        this.urlBuilder = urlBuilder;
    }

    public static final Pattern EMAIL_PATTERN = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[" +
            "a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|" +
            "\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-" +
            "z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0" +
            "-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\" +
            "x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");
}
