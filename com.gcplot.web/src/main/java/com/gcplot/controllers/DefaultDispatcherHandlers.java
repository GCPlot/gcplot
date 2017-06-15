package com.gcplot.controllers;

import com.codahale.metrics.MetricRegistry;
import com.gcplot.configuration.ConfigProperty;
import com.gcplot.commons.ErrorMessages;
import com.gcplot.commons.Metrics;
import com.gcplot.configuration.ConfigurationManager;
import com.gcplot.model.role.Restriction;
import com.gcplot.model.role.RestrictionType;
import com.gcplot.web.Dispatcher;
import com.gcplot.web.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/10/16
 */
public class DefaultDispatcherHandlers {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultDispatcherHandlers.class);

    protected Dispatcher<String> dispatcher;
    public Dispatcher<String> getDispatcher() {
        return dispatcher;
    }
    @Autowired
    public void setDispatcher(Dispatcher<String> dispatcher) {
        this.dispatcher = dispatcher;
    }

    protected ConfigurationManager config;
    public ConfigurationManager getConfig() {
        return config;
    }
    @Autowired
    public void setConfig(ConfigurationManager config) {
        this.config = config;
    }

    protected MetricRegistry metrics;
    public MetricRegistry getMetrics() {
        return metrics;
    }
    @Autowired
    public void setMetrics(MetricRegistry metrics) {
        this.metrics = metrics;
    }

    @PostConstruct
    public void init() {
        dispatcher.exceptionHandler(this::error);
        dispatcher.preHandle(this::preHandle);
        dispatcher.postHandle(this::postHandle);
    }

    public boolean preHandle(RequestContext ctx) {
        if (config.readBoolean(ConfigProperty.SERVING_DISABLED)) {
            ctx.finish(ErrorMessages.buildJson(ErrorMessages.SERVING_IS_DISABLED));
            return false;
        }
        if (ctx.loginInfo().isPresent()) {
            boolean accessRestricted = ctx.loginInfo().get().getAccount().roles()
                    .stream().flatMap(r -> r.restrictions().stream())
                    .filter(r -> r.type() == RestrictionType.TOGGLE)
                    .filter(r -> r.action().equals(ctx.path()))
                    .anyMatch(Restriction::restricted);
            if (accessRestricted) {
                ctx.write(ErrorMessages.buildJson(ErrorMessages.ACCESS_DENIED));
                return false;
            }
        }
        metrics.meter(Metrics.name("requests", query(ctx))).mark();
        return true;
    }

    public void postHandle(RequestContext request) {

    }

    public void error(Throwable t, RequestContext request) {
        LOG.error("CONTROLLER ERROR: " + dumpRequest(request), t);
        metrics.meter(Metrics.name("requests", query(request), "errors")).mark();
        if (!request.isFinished()) {
            request.clear();
            request.write(ErrorMessages.buildJson(ErrorMessages.INTERNAL_ERROR));
        }
    }

    protected String dumpRequest(RequestContext req) {
        StringBuilder sb = new StringBuilder();
        sb.append("Path [").append(req.path()).append("] ");
        sb.append("User Agent [").append(req.getUserAgent()).append("] ");
        sb.append("IP [").append(req.getIp()).append("] ");
        sb.append("Method [").append(req.method()).append("] ");
        if (req.loginInfo().isPresent()) {
            sb.append("User [").append(req.loginInfo().get().getAccount().username())
                    .append(" | ").append(req.loginInfo().get().getAccount().firstName()).append(" ")
                    .append(req.loginInfo().get().getAccount().lastName()).append("] ");
        } else {
            sb.append("Anonymous User [] ");
        }
        sb.append("Headers [");
        req.headers().forEach((k, v) -> sb.append(k).append("=").append('"').append(v).append("\";"));
        sb.append("]");
        return sb.toString();
    }

    protected String query(RequestContext request) {
        return request.path().replace("/", "_").replace(".", "_");
    }

}
