package com.gcplot.services;

import com.gcplot.configuration.ConfigProperty;
import com.gcplot.configuration.ConfigurationManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

public class UrlBuilder {
    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";
    protected ConfigurationManager config;
    private String uiProtocol;
    private String uiHost;
    private String uiNewPasswordPath;
    private String uiConfirmPath;

    public String apiUrl(String url, String... params) {
        StringBuilder sb = new StringBuilder();
        if (config.readBoolean(ConfigProperty.USE_PUBLIC_SSL)) {
            sb.append(HTTPS);
        } else {
            sb.append(HTTP);
        }
        sb.append(config.readString(ConfigProperty.API_HOST));
        if (!url.startsWith("/")) {
            sb.append("/");
        }
        sb.append(url);
        if (params.length > 0) {
            sb.append('?');
        }
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("Broken pairs of url parameters: " + Arrays.toString(params));
        }
        for (int i = 0; i < params.length; i++) {
            if (i % 2 == 0) {
                sb.append(params[i]);
            } else {
                try {
                    sb.append('=').append(URLEncoder.encode(params[i], "UTF-8"));
                } catch (UnsupportedEncodingException ignored) {
                }
                if (i < params.length - 1) {
                    sb.append('&');
                }
            }
        }
        return sb.toString();
    }

    public String uiPostConfirmUrl() {
        return uiProtocol + "://" + uiHost + uiConfirmPath;
    }

    public String uiNewPasswordPageUrl(String token, String salt) {
        return uiProtocol + "://" + uiHost + uiNewPasswordPath.replace("{TOKEN}", token).replace("{SALT}", salt);
    }

    public void setUiProtocol(String uiProtocol) {
        this.uiProtocol = uiProtocol;
    }

    public void setUiHost(String uiHost) {
        this.uiHost = uiHost;
    }

    public void setUiConfirmPath(String uiConfirmPath) {
        this.uiConfirmPath = uiConfirmPath;
    }

    public void setUiNewPasswordPath(String uiNewPasswordPath) {
        this.uiNewPasswordPath = uiNewPasswordPath;
    }

    public ConfigurationManager getConfig() {
        return config;
    }
    public void setConfig(ConfigurationManager config) {
        this.config = config;
    }
}
