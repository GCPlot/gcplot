package com.gcplot.services.mail;

import com.gcplot.configuration.ConfigProperty;
import com.gcplot.configuration.ConfigurationManager;
import com.gcplot.model.account.Account;
import com.gcplot.services.UrlBuilder;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/18/17
 */
public class MailService {
    private static final String CONFIRM_PROPERTY_NAME = "${email.confirm.template}";
    private static final String NEW_PASS_URL_PROPERTY_NAME = "${new.pass.url}";
    private MailProvider mailProvider;
    private ConfigurationManager config;
    private UrlBuilder urlBuilder;
    private boolean isAsync = false;

    public void sendConfirmationFor(Account account) {
        String template = config.readString(ConfigProperty.EMAIL_CONFIRM_TEMPLATE);
        String url = urlBuilder.apiUrl("/user/confirm", "token", account.token(), "salt", account.confirmationSalt());
        String msg = MoreObjects.firstNonNull(Strings.emptyToNull(template), CONFIRM_PROPERTY_NAME).replace(CONFIRM_PROPERTY_NAME, url);
        send(account, "Confirmation notice", msg);
    }

    public void sendNewPassUrl(Account account) {
        String template = config.readString(ConfigProperty.EMAIL_NEW_PASS_TEMPLATE);
        String url = urlBuilder.uiNewPasswordPageUrl(account.token(), account.confirmationSalt());
        String msg = MoreObjects.firstNonNull(Strings.emptyToNull(template), NEW_PASS_URL_PROPERTY_NAME).replace(NEW_PASS_URL_PROPERTY_NAME, url);
        send(account, "Password Change Request", msg);
    }

    private void send(Account account, String subject, String msg) {
        mailProvider.send(account.email(), subject, msg, isAsync);
    }

    public void setMailProvider(MailProvider mailProvider) {
        this.mailProvider = mailProvider;
    }

    public void setUrlBuilder(UrlBuilder urlBuilder) {
        this.urlBuilder = urlBuilder;
    }

    public void setConfig(ConfigurationManager config) {
        this.config = config;
    }

    public void setAsync(boolean async) {
        isAsync = async;
    }
}
