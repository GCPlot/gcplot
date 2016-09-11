package com.gcplot.parser;

import com.gcplot.model.gc.*;
import org.joda.time.DateTime;

import java.util.EnumSet;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         9/1/16
 */
public class DefaultGCEventFactory implements GCEventFactory {

    @Override
    public GCEvent create(String id, String parentId, String bucketId, DateTime occurred,
                          String description, VMEventType vmEventType, Capacity capacity,
                          Capacity totalCapacity, double timestamp, long pauseMu,
                          EnumSet<Generation> generations, EventConcurrency concurrency,
                          String ext) {
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
        event.concurrency(concurrency);
        event.ext(ext);
        return event;
    }
}
