package com.gcplot.model.gc;

import org.joda.time.DateTime;

import java.util.EnumSet;
import java.util.Map;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/7/16
 */
public interface GCEventFactory {

    GCEvent create(String id, String parentId, String bucketId, DateTime occurred, String description,
                   VMEventType vmEventType, Capacity capacity, Capacity totalCapacity, double timestamp,
                   long pauseMu, EnumSet<Generation> generations, Phase phase, EventConcurrency concurrency,
                   Map<Generation, Capacity> generationCapacityMap, String ext);

}
