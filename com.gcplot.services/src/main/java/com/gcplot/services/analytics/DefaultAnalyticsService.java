package com.gcplot.services.analytics;

import com.gcplot.Identifier;
import com.gcplot.analytics.AnalyticsService;
import com.gcplot.analytics.EventsResult;
import com.gcplot.analytics.GCEventFeature;
import com.gcplot.commons.ErrorMessages;
import com.gcplot.model.gc.analysis.GCAnalyse;
import com.gcplot.utils.Range;
import com.gcplot.model.IdentifiedEvent;
import com.gcplot.model.gc.*;
import com.gcplot.repository.GCAnalyseRepository;
import com.gcplot.repository.GCEventRepository;
import com.gcplot.commons.interceptors.EventInterceptor;
import com.gcplot.commons.interceptors.PhaseSampler;
import com.gcplot.commons.interceptors.Sampler;
import com.gcplot.services.stats.RatesInterceptor;
import com.gcplot.services.stats.StatisticAggregateInterceptor;
import it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         2/24/17
 */
public class DefaultAnalyticsService implements AnalyticsService {
    private static final Long2LongMap PERIOD_SAMPLING_BUCKETS = new Long2LongLinkedOpenHashMap() {{
        put(420L, 5L); /* 7 minutes -> 5 seconds */
        put(900L, 10L); /* 15 minutes -> 10 seconds */
        put(1800L, 15L); /* 30 minutes -> 15 seconds */
        put(2700L, 20L); /* 45 minutes -> 20 seconds */
        put(3600L, 30L); /* 1 hours -> 30 seconds */
        put(5400L, 45L); /* 1.5 hours -> 45 seconds */
        put(7200L, 60L); /* 2 hours -> 60 seconds */
        put(14600L, 80L); /* 4 hours -> 1 min 20 sec */
        put(21600L, 120L); /* 6 hours -> 2 minutes */
        put(32400L, 180L); /* 9 hours -> 3 minutes */
        put(43200L, 240L); /* 12 hours -> 4 minutes */
        put(57600L, 360L); /* 16 hours -> 6 minutes */
        put(86400L, 480L); /* 24 hours -> 8 minutes */
        put(172800L, 960L); /* 2 days -> 16 minutes */
        put(345600L, 1800L); /* 4 days -> 30 minutes */
        put(518400L, 2400L); /* 6 days -> 40 minutes */
        put(604800L, 3000L); /* 7 days -> 50 minutes */
        put(777600L, 4200L); /* 9 days -> 70 minutes */
        put(1728000L, 7200L); /* 14 days -> 2 hours */
        put(1209600L, 10800L); /* 20 days -> 3 hours */
        put(2592000L, 14400L); /* 30 days -> 4 hours */
        put(5184000L, 18000L); /* 60 days -> 5 hours */
        put(7776000L, 32000L); /* 90 days -> 8.8 hours */
    }};
    private GCAnalyseRepository analyseRepository;
    private GCEventRepository eventRepository;
    private GCEventFactory eventFactory;
    private EventsAnalyticsProcessor eventsAnalyticsProcessor;

    @Override
    public EventsResult events(Identifier accountId, String analyseId, String jvmId, Interval interval,
                               EnumSet<GCEventFeature> features, Consumer<IdentifiedEvent> listener) {
        Optional<GCAnalyse> oa = analyseRepository.analyse(accountId, analyseId);
        if (!oa.isPresent()) {
            return new EventsResult(ErrorMessages.buildJson(ErrorMessages.UNKNOWN_GC_ANALYZE, "Unknown Analyse " + analyseId));
        }
        GCAnalyse analyse = oa.get();
        Range range = correctIntervalIfRequired(jvmId, Range.of(interval), analyse);
        long secondsBetween = new Duration(range.from(), range.to()).getStandardSeconds();
        int sampleSeconds = pickUpSampling(secondsBetween);

        Iterator<GCEvent> i = eventRepository.lazyEvents(analyseId, jvmId, range);
        List<EventInterceptor<GCEvent>> samplers = buildSamplers(features, sampleSeconds);
        List<EventInterceptor> interceptors = buildInterceptors(features, sampleSeconds, isG1(jvmId, analyse));
        eventsAnalyticsProcessor.processEvents(listener, sampleSeconds, i, samplers, interceptors);

        return EventsResult.SUCCESS;
    }

    private List<EventInterceptor> buildInterceptors(EnumSet<GCEventFeature> features, int sampleSeconds, boolean isG1) {
        boolean hasRates = features.contains(GCEventFeature.CALC_RATES);
        boolean hasStats = features.contains(GCEventFeature.CALC_STATISTIC);
        if (!hasRates && !hasStats) {
            return Collections.emptyList();
        }
        List<EventInterceptor> interceptors = new ArrayList<>((hasRates ? 1 : 0) + (hasStats ? 1 : 0));
        if (hasRates) {
            interceptors.add(new RatesInterceptor(sampleSeconds));
        }
        if (hasStats) {
            interceptors.add(new StatisticAggregateInterceptor(isG1));
        }
        return interceptors;
    }

    private List<EventInterceptor<GCEvent>> buildSamplers(EnumSet<GCEventFeature> features, int sampleSeconds) {
        List<EventInterceptor<GCEvent>> interceptors = new ArrayList<>(4);
        if (features.contains(GCEventFeature.SAMPLE_YOUNG)) {
            interceptors.add(new Sampler(sampleSeconds, GCEvent::isYoung));
        }
        if (features.contains(GCEventFeature.SAMPLE_TENURED)) {
            interceptors.add(new PhaseSampler(sampleSeconds, e -> e.isTenured() && e.concurrency() == EventConcurrency.SERIAL));
        }
        if (features.contains(GCEventFeature.SAMPLE_CONCURRENT)) {
            interceptors.add(new PhaseSampler(sampleSeconds, e -> e.concurrency() == EventConcurrency.CONCURRENT));
        }
        if (features.contains(GCEventFeature.SAMPLE_FULL)) {
            interceptors.add(new Sampler(sampleSeconds, GCEvent::isFull));
        }
        return interceptors;
    }

    private int pickUpSampling(long seconds) {
        return pickUpSampling(seconds, 420, 32000, PERIOD_SAMPLING_BUCKETS);
    }

    private int pickUpSampling(long seconds, long start, int last, Long2LongMap buckets) {
        if (seconds < start) {
            return 1;
        }
        long sample = 0;
        for (Long2LongMap.Entry e : buckets.long2LongEntrySet()) {
            if (seconds < e.getLongKey()) {
                sample = e.getLongValue();
                break;
            }
        }
        if (sample == 0) {
            return last;
        }
        return (int) sample;
    }

    private boolean isG1(String jvmId, GCAnalyse analyse) {
        return analyse.jvmGCTypes().get(jvmId) == GarbageCollectorType.ORACLE_G1;
    }

    private Range correctIntervalIfRequired(String jvmId, Range interval, GCAnalyse analyse) {
        DateTime firstEvent = analyse.firstEvent().get(jvmId);
        DateTime lastEvent = analyse.lastEvent().get(jvmId);
        if (!interval.from().isBefore(firstEvent)) {
            firstEvent = interval.from();
        }
        if (!interval.to().isAfter(lastEvent)) {
            lastEvent = interval.to();
        }
        return new Range(firstEvent, lastEvent);
    }

    public GCAnalyseRepository getAnalyseRepository() {
        return analyseRepository;
    }

    public void setAnalyseRepository(GCAnalyseRepository analyseRepository) {
        this.analyseRepository = analyseRepository;
    }

    public GCEventRepository getEventRepository() {
        return eventRepository;
    }

    public void setEventRepository(GCEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public GCEventFactory getEventFactory() {
        return eventFactory;
    }

    public void setEventFactory(GCEventFactory eventFactory) {
        this.eventFactory = eventFactory;
    }

    public EventsAnalyticsProcessor getEventsAnalyticsProcessor() {
        return eventsAnalyticsProcessor;
    }

    public void setEventsAnalyticsProcessor(EventsAnalyticsProcessor eventsAnalyticsProcessor) {
        this.eventsAnalyticsProcessor = eventsAnalyticsProcessor;
    }
}
