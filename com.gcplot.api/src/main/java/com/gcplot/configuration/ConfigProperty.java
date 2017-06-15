package com.gcplot.configuration;

import java.util.HashMap;
import java.util.Map;

public enum ConfigProperty {
    SERVING_DISABLED("serving.disabled", false),
    POLL_INTERVAL("config.poll.interval", 15000),

    PUBLIC_HOST("public.host.domain", "gcplot.com"),
    API_HOST("api.host.domain", "api.gcplot.com"),
    USE_PUBLIC_SSL("use.public.ssl", false),

    EMAIL_CONFIRM_TEMPLATE("email.confirm.template", ""),
    EMAIL_NEW_PASS_TEMPLATE("email.new.pass.template", ""),
    EMAIL_HOST_NAME("email.host.name", "smtp.yandex.ru"),
    EMAIL_SMTP_PORT("email.smtp.port", 465),
    EMAIL_USE_SSL("email.use.ssl", true),
    EMAIL_AUTH("email.auth", true),
    EMAIL_CONNECTION_TIMEOUT("email.connection.timeout", 10000),
    EMAIL_SEND_TIMEOUT("email.send.timeout", 5000),
    EMAIL_DEFAULT_USERNAME("email.confirm.username", "do-not-reply@gcplot.com"),
    EMAIL_DEFAULT_FROM("email.confirm.from", "do-not-reply@gcplot.com"),
    EMAIL_DEFAULT_PASSWORD("email.confirm.password", "%*KmX_bh/Z:e7)u\""),

    TEST1_CONFIG("test.config.1", "test"),

    BATCH_PUT_GC_EVENT_SIZE("batch.put.gc.event.size", 5),
    USER_ANALYSIS_COUNT_CACHE_SIZE("user.analysis.cache.size", 5_000L),
    USER_ANALYSIS_COUNT_CACHE_SECONDS("user.analysis.cache.min", 120L),
    ANALYSIS_STATISTIC_CACHE_SIZE("analysis.stats.cache.size", 10_000L),
    ANALYSIS_STATISTIC_CACHE_SECONDS("analysis.stats.cache.min", 300L),
    GC_EVENTS_MAX_INTERVAL_DAYS("user.gc.events.max.interval.days", 90),
    SURVIVOR_AGES_AVG_THRESHOLD("survivor.ages.avg.threshold", 100),
    TENURED_ACCUMULATE_SECONDS("tenured.accumulate.seconds", 10),
    FORBID_OTHER_GENERATION("forbid.other.generation", true),

    PASSWORD_MIN_LENGTH("password.min.length", 1),

    CONFIRMATION_IS_RESTRICTED("confirmation.is.restricted", false),

    PARSE_LOG_MAX_FILE_SIZE("parse.log.max.file.size", 512L * 1024 * 1024),
    PARSE_LOG_SAMPLING_SECONDS("parse.log.sampling.seconds", 60 * 1024),

    CONNECTOR_LATEST_VERSION("connector.latest.version", "0.0.1");

    private static Map<String, ConfigProperty> types = new HashMap<>();

    static {
        for (ConfigProperty cp : ConfigProperty.values()) {
            types.put(cp.getKey(), cp);
        }
    }

    public static ConfigProperty get(String key) {
        return types.get(key);
    }

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
