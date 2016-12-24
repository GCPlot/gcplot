package com.gcplot.interceptors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.commons.serialization.JsonSerializer;
import com.gcplot.interceptors.stats.GCStats;
import com.gcplot.interceptors.stats.MinMaxAvg;
import com.gcplot.model.gc.*;
import com.gcplot.web.RequestContext;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class StatisticAggregateInterceptor extends BaseInterceptor implements Interceptor {
    private static final Logger LOG = LoggerFactory.getLogger(StatisticAggregateInterceptor.class);
    private static final Function<Integer, MinMaxAvg> MIN_MAX_FACTORY = k -> new MinMaxAvg();
    public static final Function<Integer, GCStats> STATS_INTERVAL_FACTORY = k -> new GCStats(true);
    public static final Function<Integer, GCStats> STATS_FACTORY = k -> new GCStats();
    private final boolean isG1;
    private GCEvent lastYoungEvent;
    @JsonProperty("generation_total")
    private Map<Integer, MinMaxAvg> generationsTotalSizes = new HashMap<>();
    @JsonProperty("generation_usage")
    private Map<Integer, MinMaxAvg> generationsUsageSizes = new HashMap<>();
    @JsonProperty("generation_stats")
    private Map<Integer, GCStats> byGeneration = new HashMap<>();
    @JsonProperty("phase_stats")
    private Map<Integer, GCStats> byPhase = new HashMap<>();
    @JsonProperty("cause_stats_y")
    private Map<Integer, Integer> youngCauseStats = new HashMap<>();
    @JsonProperty("stats")
    private GCStats stats = new GCStats();
    @JsonProperty("full_stats")
    private GCStats fullStats = new GCStats();
    @JsonProperty("heap_total")
    private MinMaxAvg heapTotal = new MinMaxAvg();
    @JsonProperty("heap_usage")
    private MinMaxAvg heapUsage = new MinMaxAvg();
    @JsonProperty("first_event")
    private long firstEvent;
    @JsonProperty("last_event")
    private long lastEvent;

    public StatisticAggregateInterceptor(boolean isG1) {
        this.isG1 = isG1;
    }

    @JsonProperty("allocation_rate")
    public long allocationRate() {
        if (allocationRateSum > 0 && allocationRateCount > 0) {
            return allocationRateSum / allocationRateCount;
        } else {
            return 0;
        }
    }

    @JsonProperty("allocated_total")
    public long allocatedTotal() {
        return allocatedSum;
    }

    @JsonProperty("promotion_rate")
    public long promotionRate() {
        if (promotionRateSum > 0 && promotionRateCount > 0) {
            return promotionRateSum / promotionRateCount;
        } else {
            return 0;
        }
    }

    @JsonProperty("promoted_total")
    public long promotedTotal() {
        return promotedSum;
    }

    @Override
    public void process(GCEvent event, Runnable delimit, RequestContext ctx) {
        if (event.generations().size() == 0) {
            LOG.debug("Event with no generation: " + event);
            return;
        }
        Generation g = event.generations().iterator().next();
        boolean isYoung = event.isYoung();
        if (isYoung) {
            youngCauseStats.merge(event.cause().type(), 0, (o, v) -> v + 1);
        }
        if (!isG1 && isYoung && lastYoungEvent != null) {
            long tenuredPrev = event.totalCapacity().usedAfter() - event.capacity().usedAfter();
            if (tenuredPrev >= 0) {
                generationsUsageSizes.computeIfAbsent(Generation.TENURED.type(), MIN_MAX_FACTORY).next(tenuredPrev);
            }

            long tenuredAfter = lastYoungEvent.totalCapacity().usedAfter() - lastYoungEvent.capacity().usedAfter();
            long afterPromoted = Math.abs((lastYoungEvent.totalCapacity().usedBefore() - lastYoungEvent.totalCapacity().usedAfter())
                    - (lastYoungEvent.capacity().usedBefore() - lastYoungEvent.capacity().usedAfter()));
            if (tenuredAfter - afterPromoted < tenuredPrev) {
                long freedTenured = tenuredPrev - (tenuredAfter - afterPromoted);
                GCStats gcs = byGeneration.computeIfAbsent(Generation.TENURED.type(), STATS_INTERVAL_FACTORY);
                gcs.nextFreedMemory(null, null, freedTenured);
                stats.nextFreedMemory(null, null, freedTenured);
            }
        }
        if (!event.capacity().equals(Capacity.NONE)) {
            // just min/max/avg usage and total sizes of generations
            calcMemoryFootprint(event, g);
        }
        calcGCStats(event, g);

        if (event.isYoung()) {
            if (firstEvent == 0) {
                firstEvent = event.occurred().getMillis();
            }
            lastEvent = event.occurred().getMillis();
            countRates(event);
        }

        if (!event.totalCapacity().equals(Capacity.NONE)) {
            heapTotal.next(event.totalCapacity().total());
            heapUsage.next(event.totalCapacity().usedBefore());
        }
        if (isYoung) {
            lastYoungEvent = event;
        }
    }

    @Override
    public void complete(Runnable delimit, RequestContext ctx) {
        ctx.write(JsonSerializer.serialize(this));
        delimit.run();
    }

    private void calcGCStats(GCEvent event, Generation g) {
        if (event.concurrency() != EventConcurrency.CONCURRENT) {
            if (event.isSingle()) {
                GCStats gcs = byGeneration.computeIfAbsent(g.type(), STATS_INTERVAL_FACTORY);
                if (!event.isTenured() || isG1) {
                    gcs.nextFreedMemory(event, 0);
                    stats.nextFreedMemory(event, 0);
                }
                gcs.nextInterval(event).nextPause(event).incrementTotal();
                stats.nextPause(event).nextInterval(event).incrementTotal();
            } else {
                for (Generation gg : event.generations()) {
                    GCStats gcStats = byGeneration.computeIfAbsent(gg.type(), STATS_INTERVAL_FACTORY);
                    gcStats.incrementTotal();
                    if (gg != Generation.TENURED || isG1) {
                        Capacity capacity = event.capacityByGeneration().get(gg);
                        if (capacity != null && !capacity.equals(Capacity.NONE)) {
                            gcStats.nextFreedMemory(capacity, event, 0);
                        }
                    }
                }
                fullStats.nextPause(event).nextInterval(event).nextFreedMemory(event, 0).incrementTotal();
            }
        } else if (event.isSingle()) {
            GCStats gcs = byPhase.computeIfAbsent(event.phase().type(), STATS_FACTORY);
            gcs.nextInterval(event).nextPause(event).incrementTotal();
        }
    }

    protected void calcMemoryFootprint(GCEvent event, Generation g) {
        if (event.isSingle()) {
            generationsUsageSizes.computeIfAbsent(g.type(), MIN_MAX_FACTORY).next(event.capacity().usedBefore());
            generationsTotalSizes.computeIfAbsent(g.type(), MIN_MAX_FACTORY).next(event.capacity().total());
        } else {
            for (Generation gg : event.generations()) {
                Capacity c = event.capacityByGeneration().get(gg);
                if (c != null && !c.equals(Capacity.NONE)) {
                    generationsUsageSizes.computeIfAbsent(gg.type(), MIN_MAX_FACTORY).next(c.usedBefore());
                    generationsTotalSizes.computeIfAbsent(gg.type(), MIN_MAX_FACTORY).next(c.total());
                }
            }
        }
    }

}
