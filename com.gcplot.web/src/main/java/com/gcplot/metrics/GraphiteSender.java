package com.gcplot.metrics;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.PickledGraphite;
import com.gcplot.services.SwitchableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class GraphiteSender extends SwitchableService {

    @Override
    public void start() {
        LOG.info("Starting Graphite reporter to [host={}; port={}]", host, port);
        graphite = new PickledGraphite(new InetSocketAddress(host, port));
        reporter = GraphiteReporter.forRegistry(registry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .build(graphite);
        reporter.start(reportEveryMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        try {
            LOG.info("Stopping Graphite reporter.");
            reporter.stop();
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
    }

    protected MetricRegistry registry;
    public MetricRegistry getRegistry() {
        return registry;
    }
    public void setRegistry(MetricRegistry registry) {
        this.registry = registry;
    }

    protected String host;
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }

    protected int port;
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }

    protected long reportEveryMillis;
    public long getReportEveryMillis() {
        return reportEveryMillis;
    }
    public void setReportEveryMillis(long reportEveryMillis) {
        this.reportEveryMillis = reportEveryMillis;
    }

    protected PickledGraphite graphite;
    protected GraphiteReporter reporter;
    protected static final Logger LOG = LoggerFactory.getLogger(GraphiteSender.class);
}
