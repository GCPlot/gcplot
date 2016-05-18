package com.gcplot.model.gc;

import org.joda.time.DateTime;

import java.util.EnumSet;
import java.util.OptionalLong;

public interface GCEvent {

    long id();

    OptionalLong parentEvent();

    long analyseId();

    String description();

    DateTime timestamp();

    VMEventType vmEventType();

    Capacity capacity();

    Capacity totalCapacity();

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

    EventConcurrency concurrency();

}
