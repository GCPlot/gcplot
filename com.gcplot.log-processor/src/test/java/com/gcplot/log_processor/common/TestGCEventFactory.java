package com.gcplot.log_processor.common;

import com.gcplot.model.VMEvent;
import com.gcplot.model.gc.*;
import org.joda.time.DateTime;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/8/16
 */
public class TestGCEventFactory implements GCEventFactory {

    @Override
    public GCEvent create(String id, String parentId, String bucketId, DateTime occurred, String description, VMEventType vmEventType,
                          Capacity capacity, Capacity totalCapacity, double timestamp, long pauseMu, double user, double sys, double real,
                          EnumSet<Generation> generations, Phase phase, Cause cause, long properties, EventConcurrency concurrency,
                          Map<Generation, Capacity> generationCapacityMap, String ext) {
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
            public String bucketId() {
                return bucketId;
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
            public Phase phase() {
                return phase;
            }

            @Override
            public Cause cause() {
                return cause;
            }

            @Override
            public long properties() {
                return properties;
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
            public double user() {
                return user;
            }

            @Override
            public double sys() {
                return sys;
            }

            @Override
            public double real() {
                return real;
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
            public Map<Generation, Capacity> capacityByGeneration() {
                return generationCapacityMap;
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
            public VMEvent jvmId(String jvmId) {
                return null;
            }

            @Override
            public String analyseId() {
                return null;
            }

            @Override
            public VMEvent analyseId(String analyseId) {
                return null;
            }

            @Override
            public String toString() {
                final StringBuffer sb = new StringBuffer("GCEvent{");
                sb.append("id='").append(id()).append('\'');
                sb.append(", parentEvent=").append(parentEvent());
                sb.append(", bucketId='").append(bucketId()).append('\'');
                sb.append(", description='").append(description()).append('\'');
                sb.append(", vmEventType=").append(vmEventType());
                sb.append(", phase=").append(phase());
                sb.append(", cause=").append(cause());
                sb.append(", properties=").append(properties());
                sb.append(", capacity=").append(capacity());
                sb.append(", totalCapacity=").append(totalCapacity());
                sb.append(", timestamp=").append(timestamp());
                sb.append(", pauseMu=").append(pauseMu());
                sb.append(", user=").append(user());
                sb.append(", sys=").append(sys());
                sb.append(", real=").append(real());
                sb.append(", generations=").append(generations());
                sb.append(", concurrency=").append(concurrency());
                sb.append(", ext='").append(ext()).append('\'');
                sb.append(", capacityByGeneration=").append(capacityByGeneration());
                sb.append(", occurred=").append(occurred());
                sb.append(", jvmId='").append(jvmId()).append('\'');
                sb.append(", analyseId='").append(analyseId()).append('\'');
                sb.append('}');
                return sb.toString();
            }
        };
    }

    @Override
    public GCEvent create(GCEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GCEvent create(GCEvent event, Capacity capacity, Capacity totalCapacity, long pauseMu, Phase phase) {
        throw new UnsupportedOperationException();
    }

}
