package com.gcplot.services.stats;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.commons.quantile.Quantile;
import com.gcplot.commons.quantile.QuantileEstimationCKMS;
import com.gcplot.model.GenerationStatsImpl;
import com.gcplot.model.gc.*;
import com.gcplot.model.stats.GCStatistic;
import com.gcplot.model.stats.GenerationStats;
import com.gcplot.model.stats.MinMaxAvg;
import com.gcplot.services.EventInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
public class StatisticAggregateInterceptor extends BaseInterceptor implements EventInterceptor<GCStatistic>, GCStatistic {
    private static final Logger LOG = LoggerFactory.getLogger(StatisticAggregateInterceptor.class);
    private static final Quantile[] QUANTILES = {
            new Quantile(0.50, 0.005),
            new Quantile(0.90, 0.005),
            new Quantile(0.95, 0.001),
            new Quantile(0.99, 0.001),
            new Quantile(0.999, 0.0005)};
    private static final Function<Object, MinMaxAvg> MIN_MAX_FACTORY = k -> new MinMaxAvg();
    public static final Function<Object, GenerationStatsImpl> STATS_INTERVAL_FACTORY = k -> new GenerationStatsImpl(true);
    public static final Function<Object, GenerationStatsImpl> STATS_FACTORY = k -> new GenerationStatsImpl();
    private final boolean isG1;
    private GCEvent lastYoungEvent;
    @JsonProperty("generation_total")
    @JsonFormat()
    private Map<Generation, MinMaxAvg> generationsTotalSizes = new EnumMap<>(Generation.class);
    @JsonProperty("generation_usage")
    private Map<Generation, MinMaxAvg> generationsUsageSizes = new EnumMap<>(Generation.class);
    @JsonProperty("generation_stats")
    private Map<Generation, GenerationStats> byGeneration = new EnumMap<>(Generation.class);
    @JsonProperty("phase_stats")
    private Map<Phase, GenerationStats> byPhase = new EnumMap<>(Phase.class);
    @JsonProperty("cause_stats")
    private Map<Cause, Integer> youngCauseStats = new EnumMap<>(Cause.class);
    @JsonProperty("stats")
    private GenerationStatsImpl stats = new GenerationStatsImpl();
    @JsonProperty("full_stats")
    private GenerationStatsImpl fullStats = new GenerationStatsImpl();
    @JsonProperty("heap_total")
    private MinMaxAvg heapTotal = new MinMaxAvg();
    @JsonProperty("heap_usage")
    private MinMaxAvg heapUsage = new MinMaxAvg();
    @JsonProperty("first_event")
    private long firstEvent;
    @JsonProperty("last_event")
    private long lastEvent;

    private QuantileEstimationCKMS qes = new QuantileEstimationCKMS(QUANTILES);

    public StatisticAggregateInterceptor(boolean isG1) {
        this.isG1 = isG1;
    }

    @Override
    public Map<Generation, MinMaxAvg> generationsTotalSizes() {
        return generationsTotalSizes;
    }

    @Override
    public Map<Generation, MinMaxAvg> generationsUsageSizes() {
        return generationsUsageSizes;
    }

    @Override
    public Map<Generation, GenerationStats> generationStats() {
        return byGeneration;
    }

    @Override
    public Map<Phase, GenerationStats> phaseStats() {
        return byPhase;
    }

    @Override
    public Map<Cause, Integer> causeStats() {
        return youngCauseStats;
    }

    @Override
    public GenerationStats cumulativeStats() {
        return stats;
    }

    @Override
    public GenerationStats fullStats() {
        return fullStats;
    }

    @Override
    public MinMaxAvg heapTotalBytes() {
        return heapTotal;
    }

    @Override
    public MinMaxAvg heapUsageBytes() {
        return heapUsage;
    }

    @Override
    public long firstEventTime() {
        return firstEvent;
    }

    @Override
    public long lastEventTime() {
        return lastEvent;
    }

    @JsonProperty("percentiles")
    public Map<Double, Long> percentiles() {
        HashMap<Double, Long> percentiles = new HashMap<>();
        for (Quantile q : QUANTILES) {
            percentiles.put(q.quantile, qes.query(q.quantile));
        }
        return percentiles;
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
    public List<GCStatistic> process(GCEvent event) {
        if (event.generations().size() == 0) {
            LOG.debug("Event with no generation: " + event);
            return Collections.emptyList();
        }
        Generation g = event.generations().iterator().next();
        boolean isYoung = event.isYoung();
        youngCauseStats.merge(event.cause(), 1, (o, v) -> o + 1);
        if (!isG1 && isYoung && lastYoungEvent != null) {
            long tenuredPrev = event.totalCapacity().usedAfter() - event.capacity().usedAfter();
            if (tenuredPrev >= 0) {
                generationsUsageSizes.computeIfAbsent(Generation.TENURED, MIN_MAX_FACTORY).next(tenuredPrev);
            }

            long tenuredAfter = lastYoungEvent.totalCapacity().usedAfter() - lastYoungEvent.capacity().usedAfter();
            long afterPromoted = Math.abs((lastYoungEvent.totalCapacity().usedBefore() - lastYoungEvent.totalCapacity().usedAfter())
                    - (lastYoungEvent.capacity().usedBefore() - lastYoungEvent.capacity().usedAfter()));
            if (tenuredAfter - afterPromoted < tenuredPrev) {
                long freedTenured = tenuredPrev - (tenuredAfter - afterPromoted);
                GenerationStatsImpl gcs = (GenerationStatsImpl) byGeneration.computeIfAbsent(Generation.TENURED, STATS_INTERVAL_FACTORY);
                gcs.nextFreedMemory(null, null, freedTenured);
                stats.nextFreedMemory(null, null, freedTenured);
            }
        }
        if (!event.capacity().equals(Capacity.NONE)) {
            // just min/max/avg usage and total sizes of generations
            calcMemoryFootprint(event, g);
        }
        calcGCStats(event, g);

        if (event.isYoung() || (event.isFull() && event.hasYoungCapacity())) {
            if (firstEvent == 0) {
                firstEvent = event.occurred().getMillis();
            }
            lastEvent = event.occurred().getMillis();
            countRates(event);
        }

        if (event.concurrency() == EventConcurrency.SERIAL) {
            qes.insert(event.pauseMu());
        }

        if (!event.totalCapacity().equals(Capacity.NONE)) {
            heapTotal.next(event.totalCapacity().total());
            heapUsage.next(event.totalCapacity().usedBefore());
        }
        if (isYoung) {
            lastYoungEvent = event;
        }
        return Collections.emptyList();
    }

    @Override
    public List<GCStatistic> complete() {
        return Collections.singletonList(this);
    }

    private void calcGCStats(GCEvent event, Generation g) {
        if (event.concurrency() != EventConcurrency.CONCURRENT) {
            if (event.isSingle()) {
                GenerationStatsImpl gcs = (GenerationStatsImpl) byGeneration.computeIfAbsent(g, STATS_INTERVAL_FACTORY);
                if (!event.isTenured() || isG1) {
                    gcs.nextFreedMemory(event, 0);
                    stats.nextFreedMemory(event, 0);
                }
                gcs.nextInterval(event).nextPause(event).incrementTotal();
                stats.nextPause(event).nextInterval(event).incrementTotal();
            } else {
                for (Generation gg : event.generations()) {
                    GenerationStatsImpl gcStats = (GenerationStatsImpl) byGeneration.computeIfAbsent(gg, STATS_INTERVAL_FACTORY);
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
            GenerationStatsImpl gcs = (GenerationStatsImpl) byPhase.computeIfAbsent(event.phase(), STATS_FACTORY);
            gcs.nextInterval(event).nextPause(event).incrementTotal();
        }
    }

    protected void calcMemoryFootprint(GCEvent event, Generation g) {
        if (event.isSingle()) {
            generationsUsageSizes.computeIfAbsent(g, MIN_MAX_FACTORY).next(event.capacity().usedBefore());
            generationsTotalSizes.computeIfAbsent(g, MIN_MAX_FACTORY).next(event.capacity().total());
        } else {
            for (Generation gg : event.generations()) {
                Capacity c = event.capacityByGeneration().get(gg);
                if (c != null && !c.equals(Capacity.NONE)) {
                    generationsUsageSizes.computeIfAbsent(gg, MIN_MAX_FACTORY).next(c.usedBefore());
                    generationsTotalSizes.computeIfAbsent(gg, MIN_MAX_FACTORY).next(c.total());
                }
            }
        }
    }

}
