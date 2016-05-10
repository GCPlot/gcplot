package com.gcplot.controllers;

import com.codahale.metrics.MetricRegistry;
import com.gcplot.commons.ErrorMessages;
import com.gcplot.commons.Metrics;
import com.gcplot.web.Dispatcher;
import com.gcplot.web.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PreDestroy;
import java.io.IOException;

public abstract class Controller {

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

    public void init() {
        dispatcher.exceptionHandler(this::error);
        dispatcher.preHandle(this::preHandle);
        dispatcher.postHandle(this::postHandle);
    }

    public void preHandle(RequestContext request) {
        metrics.meter(Metrics.name("requests", query(request))).mark();
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

    @PreDestroy
    public void shutdown() {
        try {
            dispatcher.close();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    protected String dumpRequest(RequestContext req) {
        StringBuilder sb = new StringBuilder();
        sb.append("Path [").append(req.path()).append("] ");
        sb.append("User Agent [").append(req.getUserAgent()).append("] ");
        sb.append("IP [").append(req.getIp()).append("] ");
        sb.append("Method [").append(req.method()).append("] ");
        sb.append("Headers [");
        req.headers().forEach((k, v) -> sb.append(k).append("=").append('"').append(v).append("\";"));
        sb.append("]");
        return sb.toString();
    }

    protected String query(RequestContext request) {
        return request.path().replace("/", "_").replace(".", "_");
    }

    protected static final Logger LOG = LoggerFactory.getLogger(Controller.class);
}
