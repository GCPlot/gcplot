package com.gcplot.controllers;

import com.gcplot.commons.ConfigProperty;
import com.gcplot.configuration.ConfigurationManager;
import com.gcplot.messages.ConnectorSettingsMessage;
import com.gcplot.web.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/28/17
 */
public class ConnectorController extends Controller {
    @Autowired
    private ConfigurationManager config;
    private String connectorS3Base;
    private String connectorS3Bucket;
    private String connectorS3AccessKey;
    private String connectorS3SecretKey;
    private String connectorS3Region;

    @PostConstruct
    public void init() {
        dispatcher.requireAuth()
                .get("/connector/internal/settings", this::getInternalConnectorSettings);
        dispatcher.noAuth()
                .get("/connector/version/latest", this::getLatestConnectorVersion);
    }

    /**
     * GET /connector/version/latest
     */
    private void getLatestConnectorVersion(RequestContext ctx) {
        ctx.write(config.readString(ConfigProperty.CONNECTOR_LATEST_VERSION));
    }

    /**
     * GET /connector/settings
     * Require Auth (token)
     */
    public void getInternalConnectorSettings(RequestContext ctx) {
        ctx.response(new ConnectorSettingsMessage(connectorS3Bucket, connectorS3Base, connectorS3AccessKey,
                connectorS3SecretKey, connectorS3Region));
    }

    public void setConnectorS3Base(String connectorS3Base) {
        this.connectorS3Base = connectorS3Base;
    }

    public void setConnectorS3Bucket(String connectorS3Bucket) {
        this.connectorS3Bucket = connectorS3Bucket;
    }

    public void setConnectorS3AccessKey(String connectorS3AccessKey) {
        this.connectorS3AccessKey = connectorS3AccessKey;
    }

    public void setConnectorS3SecretKey(String connectorS3SecretKey) {
        this.connectorS3SecretKey = connectorS3SecretKey;
    }

    public void setConnectorS3Region(String connectorS3Region) {
        this.connectorS3Region = connectorS3Region;
    }
}
