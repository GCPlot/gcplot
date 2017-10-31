package com.gcplot.services.logs.interceptors;

import com.gcplot.logs.IdentifiedEventInterceptor;
import com.gcplot.model.IdentifiedEvent;
import com.gcplot.model.gc.EventConcurrency;
import com.gcplot.model.gc.GCRate;
import com.gcplot.model.gc.analysis.ConfigProperty;
import com.gcplot.model.gc.analysis.GCAnalyse;
import com.gcplot.model.gc.GCEvent;
import com.gcplot.services.network.GraphiteSender;
import com.gcplot.services.network.ProxyConfiguration;
import com.gcplot.services.network.ProxyType;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
            fillSTWPauses(gcEvent, prefix, metrics);
            fillConcurrentPauses(gcEvent, prefix, metrics);
            fillMemory(gcEvent, prefix, metrics);
        } else if (event.isGCRate()) {
            GCRate rate = (GCRate) event;
            fillGCRate(rate, prefix, metrics);
        }
    }

    @Override
    public void finish() {
        for (String url : urls) {
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

    private void fillConcurrentPauses(GCEvent gcEvent, String prefix, Map<String, Long> m) {
        if (gcEvent.concurrency() == EventConcurrency.CONCURRENT) {
            String p = prefix + "pauses.concurrent.";
            switch (gcEvent.phase()) {
                case G1_REMARK: {
                    m.put(p + "g1_remark.ms " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
                    break;
                }
                case G1_CLEANUP: {
                    m.put(p + "g1_cleanup.ms " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
                    break;
                }
                case G1_COPYING: {
                    m.put(p + "g1_copy.ms " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
                    break;
                }
                case G1_INITIAL_MARK: {
                    m.put(p + "g1_init_mark.ms " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
                    break;
                }
                case G1_CONCURRENT_MARKING: {
                    m.put(p + "g1_conc_mark.ms " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
                    break;
                }
                case G1_ROOT_REGION_SCANNING: {
                    m.put(p + "g1_root_scan.ms " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
                    break;
                }
                case CMS_REMARK: {
                    m.put(p + "cms_remark.ms " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
                    break;
                }
                case CMS_INITIAL_MARK: {
                    m.put(p + "cms_init_mark.ms " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
                    break;
                }
                case CMS_CONCURRENT_MARK: {
                    m.put(p + "cms_conc_mark.ms " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
                    break;
                }
                case CMS_CONCURRENT_RESET: {
                    m.put(p + "cms_conc_reset.ms " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
                    break;
                }
                case CMS_CONCURRENT_SWEEP: {
                    m.put(p + "cms_conc_sweep.ms " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
                    break;
                }
                case CMS_CONCURRENT_PRECLEAN: {
                    m.put(p + "cms_conc_pclean.ms " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
                    break;
                }
                case OTHER: {
                    m.put(p + "other.ms " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
                    break;
                }
            }
        }
    }

    private void fillSTWPauses(GCEvent gcEvent, String prefix, Map<String, Long> m) {
        if (gcEvent.concurrency() == EventConcurrency.SERIAL) {
            String p = prefix + "pauses.stw.";
            if (gcEvent.isYoung()) {
                m.put(p + "young.ms.max " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
                m.put(p + "young.ms.min " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
            } else if (gcEvent.isTenured()) {
                m.put(p + "tenured.ms.max " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
                m.put(p + "tenured.ms.min " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
            } else if (gcEvent.isFull()) {
                m.put(p + "full.ms.max " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
                m.put(p + "full.ms.min " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
            } else if (gcEvent.isPerm()) {
                m.put(p + "perm.ms.max " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
                m.put(p + "perm.ms.min " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
            } else if (gcEvent.isMetaspace()) {
                m.put(p + "metaspace.ms.max " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
                m.put(p + "metaspace.ms.min " + (gcEvent.pauseMu() / 1000), gcEvent.occurred().getMillis());
            }
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
}
