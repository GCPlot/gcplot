package com.gcplot.model.gc;

import org.joda.time.DateTime;

import java.util.EnumSet;

public interface GCEvent {

    long id();

    long analyseId();

    String description();

    DateTime timestamp();

    VMEventType vmEventType();

    /**
     * Used bytes before this GC event.
     */
    long usedBeforeBytes();

    /**
     * Used bytes after this GC event.
     */
    long afterUsedBytes();

    /**
     * Capacity in bytes.
     */
    long totalBytes();

    /**
     * Pause value in microseconds of the whole step (which
     * might include sub steps as well)
     */
    long pauseMu();

    /**
     * Duration of the event. If this event is not sub-event of some
     * other event, then duration = pause.
     */
    long durationMu();

    /**
     * Generations, affected by this event.
     */
    EnumSet<Generation> generations();

    boolean isConcurrent();

}
