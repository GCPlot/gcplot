package com.gcplot.services.analytics;

import com.gcplot.Identifier;
import com.gcplot.analytics.AnalyticsService;
import com.gcplot.analytics.EventsResult;
import com.gcplot.analytics.GCEventFeature;
import com.gcplot.commons.ErrorMessages;
import com.gcplot.commons.Range;
import com.gcplot.model.IdentifiedEvent;
import com.gcplot.model.gc.*;
import com.gcplot.repository.GCAnalyseRepository;
import com.gcplot.repository.GCEventRepository;
import com.gcplot.services.EventInterceptor;
import com.gcplot.services.filters.PhaseSampler;
import com.gcplot.services.filters.Sampler;
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
        put(3600L, 15L); /* 1 hours -> 15 seconds */
        put(7200L, 30L); /* 2 hours -> 30 seconds */
        put(21600L, 60L); /* 6 hours -> 1 minute */
        put(43200L, 120L); /* 12 hours -> 2 minutes */
        put(86400L, 240L); /* 24 hours -> 4 minutes */
        put(172800L, 600L); /* 2 days -> 10 minutes */
        put(345600L, 1200L); /* 4 days -> 20 minutes */
        put(604800L, 1800L); /* 7 days -> 30 minutes */
        put(1209600L, 3000L); /* 14 days -> 50 minutes */
        put(2592000L, 7200L); /* 30 days -> 2 hours */
        put(5184000L, 14400L); /* 60 days -> 4 hours */
        put(7776000L, 18000L); /* 90 days -> 5 hours */
    }};
    private GCAnalyseRepository analyseRepository;
    private GCEventRepository eventRepository;
    private GCEventFactory eventFactory;

    @Override
    public EventsResult events(Identifier accountId, String analyseId, String jvmId, Interval interval,
                               EnumSet<GCEventFeature> features, Consumer<IdentifiedEvent> listener) {
        Optional<GCAnalyse> oa = analyseRepository.analyse(accountId, analyseId);
        if (!oa.isPresent()) {
            return new EventsResult(ErrorMessages.buildJson(ErrorMessages.UNKNOWN_GC_ANALYSE, "Unknown Analyse " + analyseId));
        }
        GCAnalyse analyse = oa.get();
        Range range = Range.of(interval);
        if (!analyse.isContinuous()) {
            range = correctIntervalIfRequired(jvmId, range, analyse);
        }
        long secondsBetween = new Duration(range.from(), range.to()).getStandardSeconds();
        int sampleSeconds = pickUpSampling(secondsBetween);

        Iterator<GCEvent> i = eventRepository.lazyEvents(analyseId, jvmId, range);
        List<EventInterceptor<GCEvent>> samplers = buildSamplers(features, sampleSeconds);
        List<EventInterceptor> interceptors = buildInterceptors(features, sampleSeconds, isG1(jvmId, analyse));
        while (i.hasNext()) {
            GCEvent next = i.next();

            if (sampleSeconds > 1) {
                boolean wasAccepted = false;
                for (EventInterceptor<GCEvent> sampler : samplers) {
                    if (sampler.isApplicable(next)) {
                        sampler.process(next).forEach(listener);
                        wasAccepted = true;
                        break;
                    }
                }
                if (!wasAccepted) {
                    listener.accept(next);
                }
            } else {
                listener.accept(next);
            }
            interceptors.forEach(ic -> ic.process(next).forEach(listener));
        }
        if (sampleSeconds > 1) {
            samplers.forEach(s -> s.complete().forEach(listener));
        }
        interceptors.forEach(ic -> ic.complete().forEach(listener));

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
        return pickUpSampling(seconds, 3600, 18000, PERIOD_SAMPLING_BUCKETS);
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
}
