package com.gcplot.services.triggers.strategy;

import com.gcplot.configuration.ConfigurationManager;
import com.gcplot.repository.TriggerRepository;
import com.gcplot.triggers.TriggerFactory;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 9/6/17
 */
public abstract class BaseTriggerStrategy implements TriggerStrategy {
    private TriggerRepository triggerRepository;
    private TriggerFactory triggerFactory;
    private ConfigurationManager config;

    public ConfigurationManager getConfig() {
        return config;
    }

    public void setConfig(ConfigurationManager config) {
        this.config = config;
    }

    public TriggerFactory getTriggerFactory() {
        return triggerFactory;
    }

    public void setTriggerFactory(TriggerFactory triggerFactory) {
        this.triggerFactory = triggerFactory;
    }

    public TriggerRepository getTriggerRepository() {
        return triggerRepository;
    }

    public void setTriggerRepository(TriggerRepository triggerRepository) {
        this.triggerRepository = triggerRepository;
    }
}
