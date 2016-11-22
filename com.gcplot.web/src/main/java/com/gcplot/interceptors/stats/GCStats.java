package com.gcplot.interceptors.stats;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.model.gc.Capacity;
import com.gcplot.model.gc.EventConcurrency;
import com.gcplot.model.gc.GCEvent;

/**
 *  - GC Time and Count
 *  - Freed memory
 *  - Min, Max, Avg time
 *  - Min, Max, Avg interval between GCs
 *
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         11/19/16
 */
public class GCStats {
    private final boolean isRestrictedInterval;
    private long totalGCTime;
    private long totalGCCount;
    private long freedMemory;
    private MinMaxAvg pause = new MinMaxAvg();
    private DMinMaxAvg interval = new DMinMaxAvg();
    private GCEvent prevIntervalEvent;

    public GCStats() {
        this(false);
    }

    public GCStats(boolean isRestrictedInterval) {
        this.isRestrictedInterval = isRestrictedInterval;
    }

    @JsonProperty("total_time")
    public long getTotalGCTime() {
        return totalGCTime;
    }

    @JsonProperty("total_count")
    public long getTotalGCCount() {
        return totalGCCount;
    }

    @JsonProperty("freed_memory")
    public long getFreedMemory() {
        return freedMemory;
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

    public void next(GCEvent event) {
        this.next(event.capacity(), EventConcurrency.SERIAL, event);
    }

    public void next(Capacity capacity, EventConcurrency concurrency, GCEvent event) {
        if (concurrency == null || event.concurrency() == concurrency) {
            totalGCTime += event.pauseMu();
            totalGCCount++;
            long fm = Math.abs(capacity.usedAfter() - capacity.usedBefore());
            if (fm == 0) {
                fm = Math.abs(event.totalCapacity().usedAfter() - event.totalCapacity().usedBefore());
            }
            freedMemory += fm;
            pause.next(event.pauseMu());
            if (!isRestrictedInterval || !event.totalCapacity().equals(Capacity.NONE)) {
                if (prevIntervalEvent != null &&
                        (!isRestrictedInterval || prevIntervalEvent.phase().equals(event.phase()))) {
                    interval.next(Math.abs(event.occurred().getMillis() - prevIntervalEvent.occurred().getMillis()));
                }
                prevIntervalEvent = event;
            }
        }
    }

}
