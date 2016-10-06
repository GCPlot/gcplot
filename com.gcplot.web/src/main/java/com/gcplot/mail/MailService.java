package com.gcplot.mail;

import com.codahale.metrics.MetricRegistry;
import com.gcplot.commons.ConfigProperty;
import com.gcplot.commons.Metrics;
import com.gcplot.commons.exceptions.Exceptions;
import com.gcplot.configuration.ConfigurationManager;
import com.gcplot.model.account.Account;
import com.gcplot.services.UrlBuilder;
import com.google.common.base.Strings;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MailService {

    public void sendConfirmationFor(Account account) {
        Email email = new SimpleEmail();
        email.setHostName(config.readString(ConfigProperty.EMAIL_HOST_NAME));
        email.setSSLOnConnect(config.readBoolean(ConfigProperty.EMAIL_USE_SSL));
        if (config.readBoolean(ConfigProperty.EMAIL_USE_SSL)) {
            email.setSslSmtpPort(config.readString(ConfigProperty.EMAIL_SMTP_PORT));
        } else {
            email.setSmtpPort(config.readInt(ConfigProperty.EMAIL_SMTP_PORT));
        }
        if (config.readBoolean(ConfigProperty.EMAIL_AUTH)) {
            email.setAuthenticator(new DefaultAuthenticator(config.readString(ConfigProperty.EMAIL_CONFIRM_USERNAME),
                    config.readString(ConfigProperty.EMAIL_CONFIRM_PASSWORD)));
        }
        try {
            email.setFrom(config.readString(ConfigProperty.EMAIL_CONFIRM_FROM));
        } catch (EmailException e) {
            throw Exceptions.runtime(e);
        }
        email.setSocketConnectionTimeout(config.readInt(ConfigProperty.EMAIL_CONNECTION_TIMEOUT));
        email.setSocketTimeout(config.readInt(ConfigProperty.EMAIL_SEND_TIMEOUT));
        email.setSubject("Confirm your GC Plot Account");
        String template = config.readString(ConfigProperty.EMAIL_CONFIRM_TEMPLATE);
        String url = urlBuilder.apiUrl("/user/confirm", "token", account.token(), "salt", account.confirmationSalt());
        if (Strings.isNullOrEmpty(template)) {
            template = url;
        } else {
            template = template.replace(PROPERTY_NAME, url);
        }
        try {
            email.setMsg(template);
            email.addTo(account.email());
            email.addHeader("Content-Type", "text/html; charset=\"UTF-8\"");
            executorService.submit(() -> {
                try {
                    email.send();
                } catch (Throwable t) {
                    metrics.meter(ERROR_METRIC_NAME).mark();
                    LOG.error("EMAIL ERROR", t);
                }
            });
        } catch (EmailException e) {
            metrics.meter(ERROR_METRIC_NAME).mark();
            throw Exceptions.runtime(e);
        }
        metrics.meter(SUCCESS_METRIC_NAME).mark();
    }

    public void init() {
        executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    public void destroy() {
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
    }

    protected ConfigurationManager config;
    public ConfigurationManager getConfig() {
        return config;
    }
    public void setConfig(ConfigurationManager config) {
        this.config = config;
    }

    protected UrlBuilder urlBuilder;
    public UrlBuilder getUrlBuilder() {
        return urlBuilder;
    }
    public void setUrlBuilder(UrlBuilder urlBuilder) {
        this.urlBuilder = urlBuilder;
    }

    protected MetricRegistry metrics;
    public MetricRegistry getMetrics() {
        return metrics;
    }
    public void setMetrics(MetricRegistry metrics) {
        this.metrics = metrics;
    }

    protected int threadPoolSize;
    public int getThreadPoolSize() {
        return threadPoolSize;
    }
    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    protected ExecutorService executorService;
    protected static final String PROPERTY_NAME = "${email.confirm.template}";
    protected static final String SUCCESS_METRIC_NAME = Metrics.name("mail", "sent");
    protected static final String ERROR_METRIC_NAME = Metrics.name("mail", "error");
    protected static final Logger LOG = LoggerFactory.getLogger(MailService.class);
}
