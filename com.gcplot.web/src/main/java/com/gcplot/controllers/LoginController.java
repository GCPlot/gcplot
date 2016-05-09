package com.gcplot.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.accounts.Account;
import com.gcplot.accounts.AccountImpl;
import com.gcplot.accounts.AccountRepository;
import com.gcplot.commons.ErrorMessages;
import com.gcplot.commons.Utils;
import com.gcplot.commons.exceptions.NotUniqueException;
import com.gcplot.messages.LoginResult;
import com.gcplot.messages.RegisterRequest;
import com.gcplot.web.RequestContext;
import org.apache.commons.codec.digest.DigestUtils;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.regex.Pattern;

public class LoginController extends Controller {

    @PostConstruct
    @Override
    public void init() {
        super.init();
        dispatcher.noAuth().filter(c -> c.hasParam("username") && c.hasParam("password"),
                "Username and password are required!").get("/user/login", this::login);
        dispatcher.noAuth().post("/user/register", RegisterRequest.class, this::register);
        dispatcher.requireAuth().filter(c -> c.hasParam("salt"),
                "Salt should be provided!").get("/user/confirm", this::confirm);
    }

    public void login(RequestContext request) {
        String username = request.param("username");
        String password = request.param("password");

        Optional<Account> r =
                getAccountRepository().account(username, hashPass(password), guess(username));
        if (r.isPresent()) {
            request.response(LoginResult.from(r.get()));
        } else {
            request.write(ErrorMessages.buildJson(ErrorMessages.WRONG_CREDENTIALS));
        }
    }

    public void register(RegisterRequest request, RequestContext c) {
        Account newAccount = AccountImpl.createNew(request.username, request.email, DigestUtils.sha256Hex(Utils.getRandomIdentifier()),
                hashPass(request.password), DigestUtils.sha256Hex(Utils.getRandomIdentifier()));

        try {
            getAccountRepository().store(newAccount);
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

    public void confirm(RequestContext context) {
        if (getAccountRepository().confirm(context.loginInfo().get().token(), context.param("salt"))) {
            context.response(SUCCESS);
        } else {
            context.write(ErrorMessages.buildJson(ErrorMessages.INTERNAL_ERROR, String.format("Can't confirm user with token %s and salt %s", context.loginInfo().get().token(),
                    context.loginInfo().get().getAccount().confirmationSalt())));
        }
    }

    protected AccountRepository.LoginType guess(String username) {
        return EMAIL_PATTERN.matcher(username).matches() ? AccountRepository.LoginType.EMAIL
                : AccountRepository.LoginType.USERNAME;
    }

    protected String hashPass(String p) {
        return DigestUtils.sha1Hex(DigestUtils.md5(p));
    }

    public static final Pattern EMAIL_PATTERN = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[" +
            "a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|" +
            "\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-" +
            "z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0" +
            "-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\" +
            "x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");
    public static final Object SUCCESS = new Object() {
        @JsonProperty("success")
        public int success = 1;
    };
}
