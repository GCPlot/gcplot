package com.gcplot.interceptors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.commons.serialization.JsonSerializer;
import com.gcplot.interceptors.stats.GCStats;
import com.gcplot.interceptors.stats.MinMaxAvg;
import com.gcplot.model.gc.*;
import com.gcplot.web.RequestContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Things to gather:
 *
 * # Generations Min/Max sizes.
 *
 * (from) -> (to) |total|
 * where total -> allocated, from -> peak
 *
 * # By Generation, Phase and Total:
 *  - GC Time and Count
 *  - Freed memory
 *  - Min, Max, Avg time
 *  - Min, Max, Avg interval between GCs
 *
 * # Allocated and Promoted bytes, rates.
 *
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         11/17/16
 */
public class StatisticAggregateInterceptor implements Interceptor {
    private GCEvent ratePreviousEvent;
    private GCEvent statsPreviousEvent;
    private GCEvent fullStatsPreviousEvent;
    private Map<Generation, GCEvent> genPrevEvent = new HashMap<>();
    private Map<Phase, GCEvent> phasePrevEvent = new HashMap<>();
    @JsonProperty("generation_total")
    private Map<Integer, MinMaxAvg> generationsTotalSizes = new HashMap<>();
    @JsonProperty("generation_usage")
    private Map<Integer, MinMaxAvg> generationsUsageSizes = new HashMap<>();
    @JsonProperty("generation_stats")
    private Map<Integer, GCStats> byGeneration = new HashMap<>();
    @JsonProperty("phase_stats")
    private Map<Integer, GCStats> byPhase = new HashMap<>();
    @JsonProperty("stats")
    private GCStats stats = new GCStats();
    @JsonProperty("full_stats")
    private GCStats fullStats = new GCStats();
    private long allocationRateSum;
    private long allocationRateCount;
    private long promotionRateSum;
    private long promotionRateCount;

    @JsonProperty("allocation_rate")
    public long allocationRate() {
        return allocationRateSum / allocationRateCount;
    }

    @JsonProperty("promotion_rate")
    public long promotionRate() {
        return promotionRateSum / promotionRateCount;
    }

    @Override
    public void process(GCEvent event, RequestContext ctx) {
        Generation g = event.generations().iterator().next();
        if (!event.capacity().equals(Capacity.NONE)) {
            calcMemoryFootprint(event, g);
        }
        calcGCStats(event, g);

        if (event.isYoung()) {
            if (ratePreviousEvent != null) {
                long period = Math.abs(ratePreviousEvent.occurred().getMillis() - event.occurred().getMillis());
                long allocated = Math.abs(ratePreviousEvent.capacity().usedBefore() - event.capacity().usedAfter());
                allocationRateSum += 1000 * allocated / period;
                allocationRateCount++;

                long youngDecreased = Math.abs(event.capacity().usedBefore() - event.capacity().usedAfter());
                long totalDecreased = Math.abs(event.totalCapacity().usedBefore() - event.totalCapacity().usedAfter());
                long promoted = Math.abs(totalDecreased - youngDecreased);
                promotionRateSum += 1000 * promoted / period;
                promotionRateCount++;
            }
            ratePreviousEvent = event;
        }
    }

    @Override
    public void complete(RequestContext ctx) {
        ctx.write(JsonSerializer.serialize(this));
    }

    protected void calcGCStats(GCEvent event, Generation g) {
        if (event.concurrency() != EventConcurrency.CONCURRENT) {
            if (event.generations().size() == 1) {
                byGeneration.computeIfAbsent(g.type(), k -> new GCStats()).next(event, genPrevEvent.get(g));
                stats.next(event, statsPreviousEvent);

                statsPreviousEvent = event;
                genPrevEvent.put(g, event);
            } else {
                for (Generation gg : event.generations()) {
                    if (gg != Generation.YOUNG && gg != Generation.TENURED) {
                        byGeneration.computeIfAbsent(gg.type(), k -> new GCStats()).next(
                                event.capacityByGeneration().get(gg), EventConcurrency.SERIAL, event, genPrevEvent.get(gg));
                        genPrevEvent.put(gg, event);
                    }
                }
                fullStats.next(event, fullStatsPreviousEvent);
                fullStatsPreviousEvent = event;
            }

        }
        if (event.generations().size() == 1) {
            byPhase.computeIfAbsent(event.phase().type(), k -> new GCStats()).next(event.capacity(), null, event,
                    phasePrevEvent.get(event.phase()));
            phasePrevEvent.put(event.phase(), event);
        }
    }

    protected void calcMemoryFootprint(GCEvent event, Generation g) {
        Function<Integer, MinMaxAvg> factory = k -> new MinMaxAvg();
        if (event.generations().size() == 1) {
            generationsUsageSizes.computeIfAbsent(g.type(), factory).next(event.capacity().usedBefore());
            generationsTotalSizes.computeIfAbsent(g.type(), factory).next(event.capacity().total());
        } else {
            for (Generation gg : event.generations()) {
                if (gg != Generation.YOUNG && gg != Generation.TENURED) {
                    Capacity c = event.capacityByGeneration().get(gg);
                    if (c != null && c.equals(Capacity.NONE)) {
                        generationsUsageSizes.computeIfAbsent(gg.type(), factory).next(c.usedBefore());
                        generationsTotalSizes.computeIfAbsent(gg.type(), factory).next(c.total());
                    }
                }
            }
        }
    }

}
