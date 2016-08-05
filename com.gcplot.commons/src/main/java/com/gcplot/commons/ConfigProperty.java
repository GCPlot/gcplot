package com.gcplot.commons;

public enum ConfigProperty {

    POLL_INTERVAL("config.poll.interval", 15000),

    PUBLIC_HOST("public.host.domain", "gcplot.com"),
    API_HOST("api.host.domain", "api.gcplot.com"),
    USE_PUBLIC_SSL("use.public.ssl", false),

    EMAIL_CONFIRM_TEMPLATE("email.confirm.template", ""),
    EMAIL_HOST_NAME("email.host.name", "smtp.yandex.ru"),
    EMAIL_SMTP_PORT("email.smtp.port", 465),
    EMAIL_USE_SSL("email.use.ssl", true),
    EMAIL_AUTH("email.auth", true),
    EMAIL_CONNECTION_TIMEOUT("email.connection.timeout", 5000),
    EMAIL_SEND_TIMEOUT("email.send.timeout", 2000),
    EMAIL_CONFIRM_USERNAME("email.confirm.username", "support@gcplot.com"),
    EMAIL_CONFIRM_FROM("email.confirm.from", "support@gcplot.com"),
    EMAIL_CONFIRM_PASSWORD("email.confirm.password", "2w72PiKF3Q8hHH"),

    TEST1_CONFIG("test.config.1", "test"),

    SURVIVOR_AGES_AVG_THRESHOLD("survivor.ages.avg.threshold", 100);

    private String key;
    public String getKey() {
        return key;
    }

    private Object defaultValue;
    public Object getDefaultValue() {
        return defaultValue;
    }

    ConfigProperty(String key, Object defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

}
