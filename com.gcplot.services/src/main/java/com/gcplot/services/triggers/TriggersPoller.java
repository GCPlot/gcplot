package com.gcplot.services.triggers;

import com.gcplot.configuration.ConfigProperty;
import com.gcplot.configuration.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 10/17/17
 */
public class TriggersPoller {
    private static final Logger LOG = LoggerFactory.getLogger(TriggersPoller.class);
    private ConfigurationManager config;
    private TriggerService triggerService;
    private ScheduledExecutorService executor;

    public void init() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                LOG.info("Starting triggers poller.");
                if (config.readBoolean(ConfigProperty.TRIGGERS_ENABLED)) {
                    triggerService.processRealtimeAnalyzes();
                }
            } catch (Throwable t) {
                LOG.error(t.getMessage(), t);
            } finally {
                LOG.info("Triggers Poller finished.");
            }
        }, config.readInt(ConfigProperty.TRIGGERS_POLL_INTERVAL_MS),
                config.readInt(ConfigProperty.TRIGGERS_POLL_INTERVAL_MS), TimeUnit.MILLISECONDS);
    }

    public void destroy() {
        try {
            if (executor != null) {
                executor.shutdown();
                executor.awaitTermination(1, TimeUnit.SECONDS);
            }
        } catch (Throwable ignore) { }
    }

    public void setConfig(ConfigurationManager config) {
        this.config = config;
    }

    public ConfigurationManager getConfig() {
        return config;
    }

    public void setTriggerService(TriggerService triggerService) {
        this.triggerService = triggerService;
    }

    public TriggerService getTriggerService() {
        return triggerService;
    }
}
