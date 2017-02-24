package com.gcplot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.model.gc.Capacity;
import com.gcplot.model.gc.GCEvent;
import com.gcplot.model.stats.DMinMaxAvg;
import com.gcplot.model.stats.GenerationStats;
import com.gcplot.model.stats.MinMaxAvg;

/**
 *  - GC Time and Count
 *  - Freed memory
 *  - Min, Max, Avg time
 *  - Min, Max, Avg interval between GCs
 *
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         11/19/16
 */
public class GenerationStatsImpl implements GenerationStats {
    private final boolean isRestrictedInterval;
    private long totalGCSum;
    private long totalGCCount; // for totalGCSum avg calc only
    private long totalGCAmount;
    private long freedMemory;
    private MinMaxAvg pause = new MinMaxAvg();
    private DMinMaxAvg interval = new DMinMaxAvg();
    private GCEvent prevIntervalEvent;

    @Override
    @JsonProperty("pause_time")
    public long totalPauseTimeMu() {
        return totalGCSum;
    }

    @Override
    @JsonProperty("pause_count")
    public long totalPauseCount() {
        return totalGCCount;
    }

    @Override
    @JsonProperty("gc_amount")
    public long eventsCount() {
        return totalGCAmount;
    }

    @Override
    @JsonProperty("freed_memory")
    public long reclaimedBytes() {
        return freedMemory;
    }

    @Override
    public MinMaxAvg pauses() {
        return pause;
    }

    @Override
    public DMinMaxAvg intervalBetweenEvents() {
        return interval;
    }

    @JsonProperty("min_pause")
    public long getMinPause() {
        return pause.getMin();
    }

    @JsonProperty("max_pause")
    public long getMaxPause() {
        return pause.getMax();
    }

    @JsonProperty("avg_pause")
    public long getAvgPause() {
        return pause.getAvg();
    }

    @JsonProperty("min_interval")
    public double getMinInterval() {
        return interval.getMin();
    }

    @JsonProperty("max_interval")
    public double getMaxInterval() {
        return interval.getMax();
    }

    @JsonProperty("avg_interval")
    public double getAvgInterval() {
        return interval.getAvg();
    }

    public GenerationStatsImpl() {
        this(false);
    }

    public GenerationStatsImpl(boolean isRestrictedInterval) {
        this.isRestrictedInterval = isRestrictedInterval;
    }

    public GenerationStatsImpl nextFreedMemory(GCEvent event, long freedMemory) {
        return this.nextFreedMemory(event.capacity(), event, freedMemory);
    }

    public GenerationStatsImpl nextFreedMemory(Capacity capacity, GCEvent event, long freedMemory) {
        if (freedMemory <= 0) {
            freedMemory = Math.abs(capacity.usedAfter() - capacity.usedBefore());
            if (freedMemory == 0 && event.generations().size() == 1) {
                freedMemory = Math.abs(event.totalCapacity().usedAfter() - event.totalCapacity().usedBefore());
            }
        }
        this.freedMemory += freedMemory;
        return this;
    }

    public GenerationStatsImpl nextInterval(GCEvent event) {
        if (prevIntervalEvent != null &&
                (!isRestrictedInterval || prevIntervalEvent.phase().equals(event.phase()))) {
            interval.next(Math.abs(event.occurred().getMillis() - prevIntervalEvent.occurred().getMillis()));
        }
        if (prevIntervalEvent == null || prevIntervalEvent.phase().equals(event.phase())) {
            prevIntervalEvent = event;
        }
        return this;
    }

    public GenerationStatsImpl nextPause(GCEvent event) {
        totalGCSum += event.pauseMu();
        totalGCCount++;
        pause.next(event.pauseMu());
        return this;
    }

    public GenerationStatsImpl incrementTotal() {
        totalGCAmount++;
        return this;
    }
}
