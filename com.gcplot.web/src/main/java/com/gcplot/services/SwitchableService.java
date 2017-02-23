package com.gcplot.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class SwitchableService {

    public abstract void start();

    public abstract void stop();

    public void init() {
        if (isEnabled()) {
            try {
                start();
            } catch (Throwable t) {
                LOG.error("Failed to start service {}! Going to retry in next {} ms.", getClass().getSimpleName(), getRetryIntervalMs());
                if (retryIntervalMs > 0) {
                    EXECUTOR.schedule(this::init, retryIntervalMs, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    public void destroy() {
        if (isEnabled()) {
            try {
                stop();
            } catch (Throwable t) {
                LOG.error(t.getMessage(), t);
            }
        }
    }

    private boolean enabled = true;
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private long retryIntervalMs = -1;
    public long getRetryIntervalMs() {
        return retryIntervalMs;
    }
    public void setRetryIntervalMs(long retryIntervalMs) {
        this.retryIntervalMs = retryIntervalMs;
    }

    protected static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    protected static Logger LOG = LoggerFactory.getLogger(SwitchableService.class);
}
