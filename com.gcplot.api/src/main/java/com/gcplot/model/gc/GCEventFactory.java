package com.gcplot.model.gc;

import org.joda.time.DateTime;

import java.util.EnumSet;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/7/16
 */
public interface GCEventFactory {

    GCEvent create(String id, String parentId, DateTime occurred, String description, VMEventType vmEventType,
                   Capacity capacity, Capacity totalCapacity, double timestamp, long pauseMu,
                   EnumSet<Generation> generations, EventConcurrency concurrency, String ext);

}
