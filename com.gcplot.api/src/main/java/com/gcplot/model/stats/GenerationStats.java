package com.gcplot.model.stats;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         2/24/17
 */
public interface GenerationStats {

    long totalPauseTimeMu();

    long totalPauseCount();

    long eventsCount();

    long reclaimedBytes();

    MinMaxAvg pauses();

    DMinMaxAvg intervalBetweenEvents();
}
