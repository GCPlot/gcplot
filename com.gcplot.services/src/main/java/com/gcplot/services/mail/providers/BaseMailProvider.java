package com.gcplot.services.mail.providers;

import com.codahale.metrics.MetricRegistry;
import com.gcplot.commons.Metrics;
import com.gcplot.configuration.ConfigurationManager;
import com.gcplot.services.mail.MailProvider;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/17/17
 */
public abstract class BaseMailProvider implements MailProvider {
    private static final Logger LOG = LoggerFactory.getLogger(BaseMailProvider.class);
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4,
            new ThreadFactoryBuilder().setNameFormat("mail-%d").setDaemon(true).build());
    protected static final String SUCCESS_METRIC_NAME = Metrics.name("mail", "sent");
    protected static final String ERROR_METRIC_NAME = Metrics.name("mail", "error");
    protected static final String CONTENT_TYPE_HEADER = "Content-Type";
    protected static final String CONTENT_TYPE = "text/html; charset=\"UTF-8\"";
    protected ConfigurationManager config;
    protected MetricRegistry metrics;
    protected MailProvider fallbackProvider;

    @Override
    public void send(String to, String subject, String msg, boolean async) {
        try {
            if (async) {
                EXECUTOR.submit(() -> {
                   try {
                       makeSend(to, subject, msg);
                       metrics.meter(SUCCESS_METRIC_NAME).mark();
                   } catch (Throwable t) {
                       metrics.meter(ERROR_METRIC_NAME).mark();
                       LOG.error("EMAIL ERROR", t);
                       tryCallFallbackProvider(to, subject, msg, true);
                   }
                });
            } else {
                makeSend(to, subject, msg);
                metrics.meter(SUCCESS_METRIC_NAME).mark();
            }
        } catch (Throwable t) {
            metrics.meter(ERROR_METRIC_NAME).mark();
            LOG.error("EMAIL ERROR", t);
            tryCallFallbackProvider(to, subject, msg, async);
        }
    }

    private void tryCallFallbackProvider(String to, String subject, String msg, boolean async) {
        if (fallbackProvider != null) {
            try {
                fallbackProvider.send(to, subject, msg, async);
            } catch (Throwable tt) {
                metrics.meter(ERROR_METRIC_NAME).mark();
                LOG.error("EMAIL ERROR", tt);
            }
        }
    }

    abstract void makeSend(String to, String subject, String msg);

    public void setConfig(ConfigurationManager config) {
        this.config = config;
    }

    public void setMetrics(MetricRegistry metrics) {
        this.metrics = metrics;
    }

    public void setFallbackProvider(MailProvider fallbackProvider) {
        this.fallbackProvider = fallbackProvider;
    }
}
