package com.gcplot.configuration;

import java.util.HashMap;
import java.util.Map;

public enum ConfigProperty {
    SERVING_DISABLED("serving.disabled", false),
    POLL_INTERVAL("config.poll.interval", 15000),

    PUBLIC_HOST("public.host.domain", ""),
    API_HOST("api.host.domain", ""),
    USE_PUBLIC_SSL("use.public.ssl", false),

    EMAIL_CONFIRM_TEMPLATE("email.confirm.template", ""),
    EMAIL_NEW_PASS_TEMPLATE("email.new.pass.template", ""),
    EMAIL_AGENT_HEALTH_TEMPLATE("email.agent.health.template", ""),

    EMAIL_DEFAULT_FROM("email.default.from", ""),
    EMAIL_DEFAULT_FROM_NAME("email.default.from.name", ""),

    SMTP_HOST_NAME("smtp.host.name", ""),
    SMTP_PORT("smtp.port", 465),
    SMTP_USE_SSL("smtp.use.ssl", false),
    SMTP_AUTH("smtp.auth", false),
    SMTP_CONNECTION_TIMEOUT("smtp.connection.timeout", 30000),
    SMTP_SEND_TIMEOUT("smtp.send.timeout", 30000),
    SMTP_DEFAULT_USERNAME("smtp.confirm.username", ""),
    SMTP_DEFAULT_PASSWORD("smtp.confirm.password", ""),

    SES_REGION("aws.ses.region", "us-east-1"),
    SES_ACCESS_KEY("aws.ses.access.key", ""),
    SES_SECRET_KEY("aws.ses.secret.key", ""),

    TEST1_CONFIG("test.config.1", "test"),

    USER_ANALYSIS_COUNT_CACHE_SIZE("user.analysis.cache.size", 5_000L),
    USER_ANALYSIS_COUNT_CACHE_SECONDS("user.analysis.cache.min", 120L),
    ANALYSIS_STATISTIC_CACHE_SIZE("analysis.stats.cache.size", 10_000L),
    ANALYSIS_STATISTIC_CACHE_SECONDS("analysis.stats.cache.min", 300L),
    GC_EVENTS_MAX_INTERVAL_DAYS("user.gc.events.max.interval.days", 90),
    SURVIVOR_AGES_AVG_THRESHOLD("survivor.ages.avg.threshold", 100),
    TENURED_ACCUMULATE_SECONDS("tenured.accumulate.seconds", 10),
    FORBID_OTHER_GENERATION("forbid.other.generation", true),
    GC_EVENTS_INTERCEPTORS_ENABLED("gc.events.interceptors.enabled", false),
    GC_EVENTS_INTERCEPTORS_MINUTES("gc.events.interceptors.minutes", 3),
    GC_EVENTS_INTERCEPTORS_QUERY_TIME_RATIO("gc.events.interceptors.query.time.ratio", 3),
    GC_EVENTS_INTERCEPTORS_SAMPLING_SECONDS("gc.events.interceptors.sampling.seconds", 60),

    GRAPHITE_REDUNDANCY_CACHE_SIZE("graphite.redundancy.cache.size", 100_000),
    GRAPHITE_EVENT_LOOP_POOL_SIZE("graphite.event.loop.pool.size", Runtime.getRuntime().availableProcessors() * 2),
    GRAPHITE_CONNECT_TIMEOUT("graphite.connect.timeout", 60000),

    PASSWORD_MIN_LENGTH("password.min.length", 1),

    CONFIRMATION_IS_RESTRICTED("confirmation.is.restricted", false),

    PARSE_LOG_MAX_FILE_SIZE("parse.log.max.file.size", 512L * 1024 * 1024),
    PARSE_LOG_SAMPLING_SECONDS("parse.log.sampling.seconds", 0),

    TRIGGERS_POLL_INTERVAL_MS("triggers.poll.interval.ms", 60 * 1000),
    TRIGGERS_EMAIL_ENABLED("triggers.email.enabled", false),
    TRIGGERS_ENABLED("triggers.enabled", false),

    IS_ADMIN_REGISTERED("admin.registered", false),
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
