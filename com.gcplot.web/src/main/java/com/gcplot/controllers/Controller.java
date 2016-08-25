package com.gcplot.controllers;

import com.codahale.metrics.MetricRegistry;
import com.gcplot.commons.ErrorMessages;
import com.gcplot.commons.Metrics;
import com.gcplot.configuration.ConfigurationManager;
import com.gcplot.model.account.Account;
import com.gcplot.web.Dispatcher;
import com.gcplot.web.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PreDestroy;
import java.io.IOException;

public abstract class Controller {
    protected static final Logger LOG = LoggerFactory.getLogger(Controller.class);

    protected Dispatcher<String> dispatcher;
    public Dispatcher<String> getDispatcher() {
        return dispatcher;
    }
    @Autowired
    public void setDispatcher(Dispatcher<String> dispatcher) {
        this.dispatcher = dispatcher;
    }

    protected MetricRegistry metrics;
    public MetricRegistry getMetrics() {
        return metrics;
    }
    @Autowired
    public void setMetrics(MetricRegistry metrics) {
        this.metrics = metrics;
    }

    protected ConfigurationManager config;
    public ConfigurationManager getConfig() {
        return config;
    }
    @Autowired
    public void setConfig(ConfigurationManager config) {
        this.config = config;
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (dispatcher.isOpen()) {
                dispatcher.close();
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    protected String token(RequestContext context) {
        return context.loginInfo().get().token();
    }

    protected Account account(RequestContext ctx) {
        return ctx.loginInfo().get().getAccount();
    }
}
