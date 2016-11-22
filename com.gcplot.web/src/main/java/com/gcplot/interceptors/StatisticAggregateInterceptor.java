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
public class StatisticAggregateInterceptor extends BaseInterceptor implements Interceptor {
    @JsonProperty("generation_total")
    private Map<Integer, MinMaxAvg> generationsTotalSizes = new HashMap<>();
    @JsonProperty("generation_usage")
    private Map<Integer, MinMaxAvg> generationsUsageSizes = new HashMap<>();
    @JsonProperty("generation_stats")
    private Map<Integer, GCStats> byGeneration = new HashMap<>();
    @JsonProperty("phase_stats")
    private Map<Integer, GCStats> byPhase = new HashMap<>();
    @JsonProperty("stats")
    private GCStats stats = new GCStats(true);
    @JsonProperty("full_stats")
    private GCStats fullStats = new GCStats();
    @JsonProperty("heap_total")
    private MinMaxAvg heapTotal = new MinMaxAvg();
    @JsonProperty("heap_usage")
    private MinMaxAvg heapUsage = new MinMaxAvg();

    @JsonProperty("allocation_rate")
    public long allocationRate() {
        if (allocationRateSum > 0 && allocationRateCount > 0) {
            return allocationRateSum / allocationRateCount;
        } else {
            return 0;
        }
    }

    @JsonProperty("promotion_rate")
    public long promotionRate() {
        if (promotionRateSum > 0 && promotionRateCount > 0) {
            return promotionRateSum / promotionRateCount;
        } else {
            return 0;
        }
    }

    @Override
    public void process(GCEvent event, Runnable delimit, RequestContext ctx) {
        Generation g = event.generations().iterator().next();
        if (!event.capacity().equals(Capacity.NONE)) {
            calcMemoryFootprint(event, g);
        }
        calcGCStats(event, g);

        if (event.isYoung()) {
            countRates(event);
        }

        if (!event.totalCapacity().equals(Capacity.NONE)) {
            heapTotal.next(event.totalCapacity().total());
            heapUsage.next(event.totalCapacity().usedBefore());
        }
    }

    @Override
    public void complete(Runnable delimit, RequestContext ctx) {
        ctx.write(JsonSerializer.serialize(this));
        delimit.run();
    }

    protected void calcGCStats(GCEvent event, Generation g) {
        if (event.concurrency() != EventConcurrency.CONCURRENT) {
            if (event.isSingle()) {
                byGeneration.computeIfAbsent(g.type(), k -> new GCStats(true)).next(event);
                stats.next(event);
            } else {
                for (Generation gg : event.generations()) {
                    if (gg != Generation.YOUNG && gg != Generation.TENURED) {
                        byGeneration.computeIfAbsent(gg.type(), k -> new GCStats()).next(
                                event.capacityByGeneration().get(gg), EventConcurrency.SERIAL, event);
                    }
                }
                fullStats.next(event);
            }
        }
        if (event.isSingle()) {
            byPhase.computeIfAbsent(event.phase().type(), k -> new GCStats()).next(event.capacity(), null, event);
        }
    }

    protected void calcMemoryFootprint(GCEvent event, Generation g) {
        Function<Integer, MinMaxAvg> factory = k -> new MinMaxAvg();
        if (event.isSingle()) {
            generationsUsageSizes.computeIfAbsent(g.type(), factory).next(event.capacity().usedBefore());
            generationsTotalSizes.computeIfAbsent(g.type(), factory).next(event.capacity().total());
        } else {
            for (Generation gg : event.generations()) {
                if (gg != Generation.YOUNG && gg != Generation.TENURED) {
                    Capacity c = event.capacityByGeneration().get(gg);
                    if (c != null && !c.equals(Capacity.NONE)) {
                        generationsUsageSizes.computeIfAbsent(gg.type(), factory).next(c.usedBefore());
                        generationsTotalSizes.computeIfAbsent(gg.type(), factory).next(c.total());
                    }
                }
            }
        }
    }

}
