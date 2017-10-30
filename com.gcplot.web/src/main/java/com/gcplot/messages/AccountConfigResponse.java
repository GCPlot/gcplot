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

    public AccountConfigResponse() {
    }

    public static AccountConfigResponse from(Configuration<ConfigProperty> config) {
        AccountConfigResponse res = new AccountConfigResponse();
        for (ConfigProperty cp : ConfigProperty.values()) {
            switch (cp) {
                case PRELOAD_ANALYSIS_ON_PAGE_OPEN:
                    res.isPreloadAnalysisOnPageOpen = config.asBoolean(cp);
                    break;
            }
        }
        return res;
    }

}
