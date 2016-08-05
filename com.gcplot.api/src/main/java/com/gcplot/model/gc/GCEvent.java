package com.gcplot.model.gc;

import com.gcplot.model.DatedEvent;
import com.gcplot.model.JVMEvent;
import org.joda.time.DateTime;

import java.util.EnumSet;
import java.util.Optional;

public interface GCEvent extends JVMEvent, DatedEvent {

    String id();

    Optional<String> parentEvent();

    String description();

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

    String ext();

}
