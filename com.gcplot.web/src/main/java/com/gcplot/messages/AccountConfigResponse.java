package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.model.account.ConfigProperty;
import com.gcplot.model.config.Configuration;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/4/17
 */
public class AccountConfigResponse {
    @JsonProperty("preload_analysis")
    public boolean isPreloadAnalysisOnPageOpen;
    @JsonProperty("notifications_enabled")
    public boolean notificationsEnabled;
    @JsonProperty("notify_realtime_agent_health")
    public boolean notifyRealtimeAgentHealth;
    @JsonProperty("realtime_agent_inactive_seconds")
    public long realtimeAgentInactiveSeconds;

    public AccountConfigResponse() {
    }

    public static AccountConfigResponse from(Configuration<ConfigProperty> config) {
        AccountConfigResponse res = new AccountConfigResponse();
        for (ConfigProperty cp : ConfigProperty.values()) {
            switch (cp) {
                case PRELOAD_ANALYSIS_ON_PAGE_OPEN:
                    res.isPreloadAnalysisOnPageOpen = config.asBoolean(cp);
                    break;
                case NOTIFICATIONS_ENABLED:
                    res.notificationsEnabled = config.asBoolean(cp);
                    break;
                case NOTIFY_REALTIME_AGENT_HEALTH:
                    res.notifyRealtimeAgentHealth = config.asBoolean(cp);
                    break;
                case REALTIME_AGENT_INACTIVE_SECONDS:
                    res.realtimeAgentInactiveSeconds = config.asLong(cp);
                    break;
            }
        }
        return res;
    }

}
