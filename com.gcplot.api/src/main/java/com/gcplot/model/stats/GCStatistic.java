package com.gcplot.model.stats;

import com.gcplot.model.IdentifiedEvent;
import com.gcplot.model.gc.Cause;
import com.gcplot.model.gc.Generation;
import com.gcplot.model.gc.Phase;

import java.util.Map;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         2/24/17
 */
public interface GCStatistic extends IdentifiedEvent {

    Map<Generation, MinMaxAvg> generationsTotalSizes();

    Map<Generation, MinMaxAvg> generationsUsageSizes();

    Map<Generation, GenerationStats> generationStats();

    Map<Phase, GenerationStats> phaseStats();

    Map<Cause, Integer> causeStats();

    GenerationStats cumulativeStats();

    GenerationStats fullStats();

    MinMaxAvg heapTotalBytes();

    MinMaxAvg heapUsageBytes();

    long firstEventTime();

    long lastEventTime();

    Map<Double, Long> percentiles();

    long allocationRate();

    long allocatedTotal();

    long promotionRate();

    long promotedTotal();

    @Override
    default boolean isStatistic() {
        return true;
    }
}
