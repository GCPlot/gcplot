package com.gcplot.interceptors.stats;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.model.gc.Capacity;
import com.gcplot.model.gc.EventConcurrency;
import com.gcplot.model.gc.GCEvent;
import com.gcplot.model.gc.Generation;

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
    private long totalGCTime;
    private long totalGCCount;
    private long freedMemory;
    private MinMaxAvg pause = new MinMaxAvg();
    private DMinMaxAvg interval = new DMinMaxAvg();

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

    public void next(GCEvent event, GCEvent prevEvent) {
        this.next(event.capacity(), EventConcurrency.SERIAL, event, prevEvent);
    }

    public void next(Capacity capacity, EventConcurrency concurrency, GCEvent event, GCEvent prevEvent) {
        if (concurrency == null || event.concurrency() == concurrency) {
            totalGCTime += event.pauseMu();
            totalGCCount++;
            freedMemory += Math.abs(capacity.usedAfter() - capacity.usedBefore());
            pause.next(event.pauseMu());
            if (prevEvent != null) {
                interval.next(Math.abs(event.occurred().getMillis() - prevEvent.occurred().getMillis()));
            }
        }
    }

}
