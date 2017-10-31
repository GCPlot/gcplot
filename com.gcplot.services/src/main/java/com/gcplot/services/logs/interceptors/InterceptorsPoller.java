package com.gcplot.services.logs.interceptors;

import com.gcplot.analytics.AnalyticsService;
import com.gcplot.analytics.GCEventFeature;
import com.gcplot.configuration.ConfigProperty;
import com.gcplot.configuration.ConfigurationManager;
import com.gcplot.repository.GCAnalyseRepository;
import com.gcplot.services.network.GraphiteSender;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InterceptorsPoller {
    private static final Logger LOG = LoggerFactory.getLogger(InterceptorsPoller.class);
    private ScheduledExecutorService executor;
    private GraphiteSender graphiteSender;
    private ConfigurationManager config;
    private GCAnalyseRepository analyseRepository;
    private AnalyticsService analyticsService;
    private Map<String, DateTime> lastTimestamps = new HashMap<>();

    public void init() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
                    try {
                        LOG.info("Starting interceptors poller.");
                        if (config.readBoolean(ConfigProperty.GC_EVENTS_INTERCEPTORS_ENABLED)) {
                            process();
                        }
                    } catch (Throwable t) {
                        LOG.error(t.getMessage(), t);
                    } finally {
                        LOG.info("Interceptors Poller finished.");
                    }
                }, config.readInt(ConfigProperty.GC_EVENTS_INTERCEPTORS_MINUTES),
                config.readInt(ConfigProperty.GC_EVENTS_INTERCEPTORS_MINUTES), TimeUnit.MINUTES);
    }

    public void shutdown() {
        try {
            if (executor != null) {
                executor.shutdown();
                executor.awaitTermination(1, TimeUnit.SECONDS);
            }
        } catch (Throwable ignore) {
        }
    }

    private void process() {
        int queryMinutes = config.readInt(ConfigProperty.GC_EVENTS_INTERCEPTORS_MINUTES);
        int queryRatio = config.readInt(ConfigProperty.GC_EVENTS_INTERCEPTORS_QUERY_TIME_RATIO);
        int samplingSeconds = config.readInt(ConfigProperty.GC_EVENTS_INTERCEPTORS_SAMPLING_SECONDS);
        analyseRepository.analyses(true).forEach(a -> {
            a.jvmIds().forEach(jvm -> {
                try {
                    GraphiteInterceptor gi = new GraphiteInterceptor(graphiteSender, a, jvm);
                    if (gi.isActive() && a.lastEvent() != null && a.lastEvent().get(jvm) != null) {
                        LOG.info("Running graphite interceptor for {}:{} {}", a.name(), a.id(), jvm);
                        LOG.debug("Graphite Interceptor: {}", gi);

                        DateTime lastEvent = a.lastEvent().get(jvm);
                        DateTime prevLastEvent = lastTimestamps.get(jvm);
                        if (prevLastEvent == null || !prevLastEvent.equals(lastEvent)) {
                            DateTime firstEvent = prevLastEvent == null ?
                                    lastEvent.minusMinutes(queryMinutes * queryRatio) : prevLastEvent;
                            Interval i = new Interval(firstEvent.withSecondOfMinute(0), lastEvent.withSecondOfMinute(0));
                            lastTimestamps.put(jvm, lastEvent);

                            analyticsService.events(a.accountId(), a.id(), jvm, i, samplingSeconds, GCEventFeature.getNoStats(), gi::intercept);
                            gi.finish();
                        }
                    }
                } catch (Throwable t) {
                    LOG.error(t.getMessage(), t);
                }
            });
        });
    }

    public GraphiteSender getGraphiteSender() {
        return graphiteSender;
    }

    public void setGraphiteSender(GraphiteSender graphiteSender) {
        this.graphiteSender = graphiteSender;
    }

    public ConfigurationManager getConfig() {
        return config;
    }

    public void setConfig(ConfigurationManager config) {
        this.config = config;
    }

    public AnalyticsService getAnalyticsService() {
        return analyticsService;
    }

    public void setAnalyticsService(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    public GCAnalyseRepository getAnalyseRepository() {
        return analyseRepository;
    }

    public void setAnalyseRepository(GCAnalyseRepository analyseRepository) {
        this.analyseRepository = analyseRepository;
    }
}
