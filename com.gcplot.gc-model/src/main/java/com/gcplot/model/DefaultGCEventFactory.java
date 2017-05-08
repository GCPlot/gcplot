package com.gcplot.model;

import com.gcplot.model.gc.*;
import org.joda.time.DateTime;

import java.util.EnumSet;
import java.util.Map;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         9/1/16
 */
public class DefaultGCEventFactory implements GCEventFactory {

    @Override
    public GCEvent create(String id, String parentId, String bucketId, DateTime occurred, String description, VMEventType vmEventType,
                          Capacity capacity, Capacity totalCapacity, double timestamp, long pauseMu, double user, double sys, double real,
                          EnumSet<Generation> generations, Phase phase, Cause cause, long properties, EventConcurrency concurrency,
                          Map<Generation, Capacity> generationCapacityMap, String ext) {
        GCEventImpl event = new GCEventImpl();
        event.id(id);
        event.parentEvent(parentId);
        event.bucketId(bucketId);
        event.occurred(occurred);
        event.description(description);
        event.vmEventType(vmEventType);
        event.capacity(capacity);
        event.totalCapacity(totalCapacity);
        event.timestamp(timestamp);
        event.pauseMu(pauseMu);
        event.generations(generations);
        event.phase(phase);
        event.cause(cause);
        event.user(user).sys(sys).real(real);
        event.properties(properties);
        event.concurrency(concurrency);
        event.capacityByGeneration(generationCapacityMap);
        event.ext(ext);
        return event;
    }

    @Override
    public GCEvent create(GCEvent event) {
        return new GCEventImpl(event);
    }

    @Override
    public GCEvent create(GCEvent event, Capacity capacity, Capacity totalCapacity, long pauseMu, Phase phase) {
        GCEventImpl e = new GCEventImpl(event);
        e.capacity(capacity);
        e.totalCapacity(totalCapacity);
        e.pauseMu(pauseMu);
        e.phase(phase);
        return e;
    }
}
