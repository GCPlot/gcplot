package com.gcplot.model.gc;

import com.gcplot.model.DatedEvent;
import com.gcplot.model.VMEvent;

import java.util.EnumSet;
import java.util.Optional;

public interface GCEvent extends VMEvent, DatedEvent {

    String id();

    Optional<String> parentEvent();

    String bucketId();

    String description();

    VMEventType vmEventType();

    Capacity capacity();

    Capacity totalCapacity();

    double timestamp();

    /**
     * Pause value in microseconds of the whole step (which
     * might include sub steps as well)
     */
    long pauseMu();

    /**
     * Generations, affected by this event.
     */
    EnumSet<Generation> generations();

    EventConcurrency concurrency();

    String ext();

    default boolean isFull() {
        return generations().size() > 1 && generations().contains(Generation.TENURED) &&
                generations().contains(Generation.TENURED);
    }

    default boolean isMetaspace() {
        return generations().size() == 1 && generations().contains(Generation.METASPACE);
    }

    default boolean isPerm() {
        return generations().size() == 1 && generations().contains(Generation.PERM);
    }

}
