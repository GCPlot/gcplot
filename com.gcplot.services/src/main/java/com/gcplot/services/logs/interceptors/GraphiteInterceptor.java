package com.gcplot.services.logs.interceptors;

import com.gcplot.logs.IdentifiedEventInterceptor;
import com.gcplot.model.IdentifiedEvent;
import com.gcplot.model.gc.EventConcurrency;
import com.gcplot.model.gc.GCRate;
import com.gcplot.model.gc.Phase;
import com.gcplot.model.gc.analysis.ConfigProperty;
import com.gcplot.model.gc.analysis.GCAnalyse;
import com.gcplot.model.gc.GCEvent;
import com.gcplot.model.stats.MinMaxAvg;
import com.gcplot.services.network.GraphiteSender;
import com.gcplot.services.network.ProxyConfiguration;
import com.gcplot.services.network.ProxyType;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 10/22/17
 */
public class GraphiteInterceptor implements IdentifiedEventInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(GraphiteInterceptor.class);
    private final GraphiteSender graphiteSender;
    private final GCAnalyse analyse;
    private final String jvmId;
    private final String jvmName;
    private final String prefix;
    private final String[] urls;
    private final ProxyConfiguration proxyConfiguration;
    private final Map<String, Long> metrics = new HashMap<>();
    private final Map<Pair<Long, EventType>, MinMaxAvg> stwEvents = new HashMap<>();
    private final Map<Pair<Long, Phase>, MinMaxAvg> concEvents = new HashMap<>();

    public GraphiteInterceptor(GraphiteSender graphiteSender, GCAnalyse analyse, String jvmId) {
        this.graphiteSender = graphiteSender;
        this.analyse = analyse;
        this.jvmId = jvmId;
        this.urls = Arrays.stream(Strings.nullToEmpty(analyse.config().asString(ConfigProperty.GRAPHITE_URLS)).replace(" ", "").split(",")).filter(s -> !s.trim().isEmpty()).map(String::trim).toArray(String[]::new);
        if (this.urls.length > 0) {
            this.jvmName = StringUtils.replaceAll(Strings.nullToEmpty(analyse.jvmNames().get(jvmId)), "[\\.\\\\/ @#$%^&*();|<>\"'+\\-\\!\\?\\:\\;]", "_");
            String prefix = Strings.nullToEmpty(analyse.config().asString(ConfigProperty.GRAPHITE_PREFIX)).replace("${jvm_name}", jvmName);
            if (!prefix.endsWith(".")) {
                prefix += ".";
            }
            this.prefix = prefix;
            ProxyConfiguration pc = ProxyConfiguration.NONE;
            long pt = analyse.config().asLong(ConfigProperty.GRAPHITE_PROXY_TYPE);
            if (pt > 0) {
                pc = new ProxyConfiguration(ProxyType.get((int) pt), analyse.config().asString(ConfigProperty.GRAPHITE_PROXY_HOST),
                        (int) analyse.config().asLong(ConfigProperty.GRAPHITE_PROXY_PORT),
                        analyse.config().asString(ConfigProperty.GRAPHITE_PROXY_USERNAME), analyse.config().asString(ConfigProperty.GRAPHITE_PROXY_PASSWORD));
            }
            this.proxyConfiguration = pc;
        } else {
            this.jvmName = null;
            this.prefix = null;
            this.proxyConfiguration = ProxyConfiguration.NONE;
        }
    }

    @Override
    public boolean isActive() {
        return urls.length > 0;
    }

    @Override
    public void intercept(IdentifiedEvent event) {
        if (event.isGCEvent()) {
            GCEvent gcEvent = (GCEvent) event;
            if (gcEvent.concurrency() == EventConcurrency.SERIAL) {
                stwEvents.computeIfAbsent(Pair.of(gcEvent.occurred().getMillis() / 1000 / 60, EventType.from(gcEvent)), k -> new MinMaxAvg())
                        .next(gcEvent.pauseMu() / 1000);
            } else if (gcEvent.concurrency() == EventConcurrency.CONCURRENT) {
                concEvents.computeIfAbsent(Pair.of(gcEvent.occurred().getMillis() / 1000 / 60, gcEvent.phase()), k -> new MinMaxAvg())
                        .next(gcEvent.pauseMu() / 1000);
            }
        } else if (event.isGCRate()) {
            GCRate rate = (GCRate) event;
            fillGCRate(rate, prefix, metrics);
        }
    }

    @Override
    public void finish() {
        for (String url : urls) {
            stwEvents.forEach((p, m) -> fillSTWPauses(m, p.getLeft() * 1000 * 60, p.getRight(), prefix, metrics));
            concEvents.forEach((p, m) -> fillConcurrentPauses(m, p.getLeft() * 1000 * 60, p.getRight(), prefix, metrics));
            LOG.info("Sending data to graphite url: {}", url);
            graphiteSender.send(url, proxyConfiguration, metrics);
        }
    }

    private void fillGCRate(GCRate rate, String prefix, Map<String, Long> m) {
        String p = prefix + "rates.";
        m.put(p + "alloc.kb_s " + rate.allocationRate(), rate.occurred().getMillis());
        m.put(p + "alloc.mb_s " + rate.allocationRate() / 1024, rate.occurred().getMillis());
        m.put(p + "promote.kb_s " + rate.promotionRate(), rate.occurred().getMillis());
        m.put(p + "promote.mb_s " + rate.promotionRate() / 1024, rate.occurred().getMillis());
    }

    private void fillMemory(GCEvent gcEvent, String prefix, Map<String, Long> m) {
        // TODO implement
    }

    private void fillConcurrentPauses(MinMaxAvg e, long occurred, Phase c, String prefix, Map<String, Long> m) {
        String p = prefix + "pauses.concurrent.";
        switch (c) {
            case G1_REMARK: {
                m.put(p + "g1_remark.ms.min " + e.getMin(), occurred);
                m.put(p + "g1_remark.ms.max " + e.getMax(), occurred);
                break;
            }
            case G1_CLEANUP: {
                m.put(p + "g1_cleanup.ms.min " + e.getMin(), occurred);
                m.put(p + "g1_cleanup.ms.max " + e.getMax(), occurred);
                break;
            }
            case G1_COPYING: {
                m.put(p + "g1_copy.ms.min " + e.getMin(), occurred);
                m.put(p + "g1_copy.ms.max " + e.getMax(), occurred);
                break;
            }
            case G1_INITIAL_MARK: {
                m.put(p + "g1_init_mark.ms.min " + e.getMin(), occurred);
                m.put(p + "g1_init_mark.ms.max " + e.getMax(), occurred);
                break;
            }
            case G1_CONCURRENT_MARKING: {
                m.put(p + "g1_conc_mark.ms.min " + e.getMin(), occurred);
                m.put(p + "g1_conc_mark.ms.max " + e.getMax(), occurred);
                break;
            }
            case G1_ROOT_REGION_SCANNING: {
                m.put(p + "g1_root_scan.ms.min " + e.getMin(), occurred);
                m.put(p + "g1_root_scan.ms.max " + e.getMax(), occurred);
                break;
            }
            case CMS_REMARK: {
                m.put(p + "cms_remark.ms.min " + e.getMin(), occurred);
                m.put(p + "cms_remark.ms.max " + e.getMax(), occurred);
                break;
            }
            case CMS_INITIAL_MARK: {
                m.put(p + "cms_init_mark.ms.min " + e.getMin(), occurred);
                m.put(p + "cms_init_mark.ms.max " + e.getMax(), occurred);
                break;
            }
            case CMS_CONCURRENT_MARK: {
                m.put(p + "cms_conc_mark.ms.min " + e.getMin(), occurred);
                m.put(p + "cms_conc_mark.ms.max " + e.getMax(), occurred);
                break;
            }
            case CMS_CONCURRENT_RESET: {
                m.put(p + "cms_conc_reset.ms.min " + e.getMin(), occurred);
                m.put(p + "cms_conc_reset.ms.max " + e.getMax(), occurred);
                break;
            }
            case CMS_CONCURRENT_SWEEP: {
                m.put(p + "cms_conc_sweep.ms.min " + e.getMin(), occurred);
                m.put(p + "cms_conc_sweep.ms.max " + e.getMax(), occurred);
                break;
            }
            case CMS_CONCURRENT_PRECLEAN: {
                m.put(p + "cms_conc_pclean.ms.min " + e.getMin(), occurred);
                m.put(p + "cms_conc_pclean.ms.max " + e.getMax(), occurred);
                break;
            }
            case OTHER: {
                m.put(p + "other.ms.min " + e.getMin(), occurred);
                m.put(p + "other.ms.max " + e.getMax(), occurred);
                break;
            }
        }
    }

    private void fillSTWPauses(MinMaxAvg e, long occurred, EventType type, String prefix, Map<String, Long> m) {
        String p = prefix + "pauses.stw.";
        if (type == EventType.YOUNG) {
            m.put(p + "young.ms.max " + e.getMax(), occurred);
            m.put(p + "young.ms.min " + e.getMin(), occurred);
        } else if (type == EventType.TENURED) {
            m.put(p + "tenured.ms.max " + e.getMax(), occurred);
            m.put(p + "tenured.ms.min " + e.getMin(), occurred);
        } else if (type == EventType.FULL) {
            m.put(p + "full.ms.max " + e.getMax(), occurred);
            m.put(p + "full.ms.min " + e.getMin(), occurred);
        } else if (type == EventType.PERM) {
            m.put(p + "perm.ms.max " + e.getMax(), occurred);
            m.put(p + "perm.ms.min " + e.getMin(), occurred);
        } else if (type == EventType.METASPACE) {
            m.put(p + "metaspace.ms.max " + e.getMax(), occurred);
            m.put(p + "metaspace.ms.min " + e.getMin(), occurred);
        }
    }

    @Override
    public String toString() {
        return "GraphiteInterceptor{" +
                "analyse=" + analyse +
                ", jvmId='" + jvmId + '\'' +
                ", jvmName='" + jvmName + '\'' +
                ", prefix='" + prefix + '\'' +
                ", urls=" + Arrays.toString(urls) +
                ", proxyConfiguration=" + proxyConfiguration +
                '}';
    }

    enum EventType {
        YOUNG, TENURED, FULL, PERM, METASPACE;

        public static EventType from(GCEvent event) {
            if (event.isYoung()) {
                return YOUNG;
            } else if (event.isTenured()) {
                return TENURED;
            } else if (event.isFull()) {
                return FULL;
            } else if (event.isPerm()) {
                return PERM;
            } else if (event.isMetaspace()) {
                return METASPACE;
            } else {
                return null;
            }
        }
    }
}
