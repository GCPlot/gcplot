package com.gcplot.services.mail.providers;

import com.gcplot.configuration.ConfigProperty;
import com.gcplot.utils.Exceptions;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/18/17
 */
public class SMTPMailProvider extends BaseMailProvider {

    protected Email createEmail() {
        Email email = new SimpleEmail();
        email.setHostName(config.readString(ConfigProperty.SMTP_HOST_NAME));
        email.setSSLOnConnect(config.readBoolean(ConfigProperty.SMTP_USE_SSL));
        if (config.readBoolean(ConfigProperty.SMTP_USE_SSL)) {
            email.setSslSmtpPort(config.readString(ConfigProperty.SMTP_PORT));
        } else {
            email.setSmtpPort(config.readInt(ConfigProperty.SMTP_PORT));
        }
        if (config.readBoolean(ConfigProperty.SMTP_AUTH)) {
            email.setAuthenticator(new DefaultAuthenticator(config.readString(ConfigProperty.SMTP_DEFAULT_USERNAME),
                    config.readString(ConfigProperty.SMTP_DEFAULT_PASSWORD)));
        }
        try {
            email.setFrom(config.readString(ConfigProperty.EMAIL_DEFAULT_FROM));
        } catch (EmailException e) {
            throw Exceptions.runtime(e);
        }
        email.setSocketConnectionTimeout(config.readInt(ConfigProperty.SMTP_CONNECTION_TIMEOUT));
        email.setSocketTimeout(config.readInt(ConfigProperty.SMTP_SEND_TIMEOUT));
        return email;
    }

    @Override
    void makeSend(String to, String subject, String msg) {
        Email email = createEmail();
        try {
            email.addTo(to);
            email.setMsg(msg);
            email.setSubject(subject);
            email.addHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE);

            email.send();
        } catch (EmailException e) {
            throw Exceptions.runtime(e);
        }
    }

}
