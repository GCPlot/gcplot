package com.gcplot.log_processor.common;

import com.gcplot.model.gc.*;
import com.google.common.base.MoreObjects;
import org.joda.time.DateTime;

import java.util.EnumSet;
import java.util.Optional;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/8/16
 */
public class TestGCEventFactory implements GCEventFactory {

    @Override
    public GCEvent create(String id, String parentId, DateTime occurred, String description, VMEventType vmEventType,
                          Capacity capacity, Capacity totalCapacity, double timestamp, long pauseMu, EnumSet<Generation> generations,
                          EventConcurrency concurrency, String ext) {
        return new GCEvent() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public Optional<String> parentEvent() {
                return Optional.ofNullable(parentId);
            }

            @Override
            public String description() {
                return description;
            }

            @Override
            public VMEventType vmEventType() {
                return vmEventType;
            }

            @Override
            public Capacity capacity() {
                return capacity;
            }

            @Override
            public Capacity totalCapacity() {
                return totalCapacity;
            }

            @Override
            public double timestamp() {
                return timestamp;
            }

            @Override
            public long pauseMu() {
                return pauseMu;
            }

            @Override
            public EnumSet<Generation> generations() {
                return generations;
            }

            @Override
            public EventConcurrency concurrency() {
                return concurrency;
            }

            @Override
            public String ext() {
                return ext;
            }

            @Override
            public DateTime occurred() {
                return occurred;
            }

            @Override
            public String jvmId() {
                return null;
            }

            @Override
            public String analyseId() {
                return null;
            }

            @Override
            public String toString() {
                return MoreObjects.toStringHelper(this)
                        .add("id", id())
                        .add("parentEvent", parentEvent())
                        .add("description", description())
                        .add("vmEventType", vmEventType())
                        .add("capacity", capacity())
                        .add("totalCapacity", totalCapacity())
                        .add("tmstm", timestamp())
                        .add("pauseMu", pauseMu())
                        .add("generations", generations())
                        .add("concurrency", concurrency())
                        .add("ext", ext())
                        .add("occurred", occurred())
                        .add("jvmId", jvmId())
                        .add("analyseId", analyseId())
                        .toString();
            }
        };
    }

}
