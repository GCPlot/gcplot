package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.model.config.Configuration;
import com.gcplot.model.gc.analysis.ConfigProperty;

public class AnalyseConfigResponse {
    @JsonProperty("graphite_urls")
    public String graphiteUrls;
    @JsonProperty("graphite_prefix")
    public String graphitePrefix;
    @JsonProperty("graphite_proxy_type")
    public int graphiteProxyType;
    @JsonProperty("graphite_proxy_host")
    public String graphiteProxyHost;
    @JsonProperty("graphite_proxy_port")
    public int graphiteProxyPort;
    @JsonProperty("graphite_proxy_username")
    public String graphiteProxyUsername;
    @JsonProperty("graphite_proxy_password")
    public String graphiteProxyPassword;

    public AnalyseConfigResponse() {}

    public static AnalyseConfigResponse from(Configuration<ConfigProperty> config) {
        AnalyseConfigResponse res = new AnalyseConfigResponse();
        for (ConfigProperty cp : ConfigProperty.values()) {
            switch (cp) {
                case GRAPHITE_URLS: res.graphiteUrls = config.asString(cp); break;
                case GRAPHITE_PREFIX: res.graphitePrefix = config.asString(cp); break;
                case GRAPHITE_PROXY_TYPE: res.graphiteProxyType = (int) config.asLong(cp); break;
                case GRAPHITE_PROXY_HOST: res.graphiteProxyHost = config.asString(cp); break;
                case GRAPHITE_PROXY_PORT: res.graphiteProxyPort = (int) config.asLong(cp); break;
                case GRAPHITE_PROXY_USERNAME: res.graphiteProxyUsername = config.asString(cp); break;
                case GRAPHITE_PROXY_PASSWORD: res.graphiteProxyPassword = config.asString(cp); break;
                default:
            }
        }
        return res;
    }
}
