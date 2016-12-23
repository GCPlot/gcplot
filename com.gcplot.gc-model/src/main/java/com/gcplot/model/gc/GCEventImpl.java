package com.gcplot.model.gc;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;

public class GCEventImpl implements GCEvent {

    @Override
    public String id() {
        return id;
    }
    public GCEventImpl id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String jvmId() {
        return jvmId;
    }
    public GCEventImpl jvmId(String jvmId) {
        this.jvmId = jvmId;
        return this;
    }

    @Override
    public Optional<String> parentEvent() {
        return Optional.ofNullable(parentEvent);
    }
    public GCEventImpl parentEvent(String parentEvent) {
        this.parentEvent = parentEvent;
        return this;
    }

    @Override
    public String bucketId() {
        return bucketId;
    }
    public GCEventImpl bucketId(String bucketId) {
        this.bucketId = bucketId;
        return this;
    }

    @Override
    public String analyseId() {
        return analyseId;
    }
    public GCEventImpl analyseId(String analyseId) {
        this.analyseId = analyseId;
        return this;
    }

    @Override
    public String description() {
        return description;
    }
    public GCEventImpl description(String description) {
        this.description = description;
        return this;
    }

    @Override
    public DateTime occurred() {
        return occurred;
    }
    public GCEventImpl occurred(DateTime occurred) {
        this.occurred = occurred;
        return this;
    }

    @Override
    public VMEventType vmEventType() {
        return vmEventType;
    }
    public GCEventImpl vmEventType(VMEventType vmEventType) {
        this.vmEventType = vmEventType;
        return this;
    }

    @Override
    public Phase phase() {
        return phase;
    }
    public GCEventImpl phase(Phase phase) {
        this.phase = phase;
        return this;
    }

    @Override
    public Cause cause() {
        return cause;
    }
    public GCEventImpl cause(Cause cause) {
        this.cause = cause;
        return this;
    }

    @Override
    public long properties() {
        return properties;
    }
    public GCEventImpl properties(long properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public Capacity capacity() {
        return capacity;
    }
    public GCEventImpl capacity(Capacity capacity) {
        this.capacity = capacity;
        return this;
    }

    @Override
    public Capacity totalCapacity() {
        return totalCapacity;
    }
    public GCEventImpl totalCapacity(Capacity totalCapacity) {
        this.totalCapacity = totalCapacity;
        return this;
    }

    @Override
    public double timestamp() {
        return timestamp;
    }
    public GCEventImpl timestamp(double timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public long pauseMu() {
        return pauseMu;
    }
    public GCEventImpl pauseMu(long pauseMu) {
        this.pauseMu = pauseMu;
        return this;
    }

    @Override
    public EnumSet<Generation> generations() {
        return generations;
    }
    public GCEventImpl generations(EnumSet<Generation> generations) {
        this.generations = generations;
        return this;
    }

    @Override
    public EventConcurrency concurrency() {
        return concurrency;
    }
    public GCEventImpl concurrency(EventConcurrency concurrency) {
        this.concurrency = concurrency;
        return this;
    }

    @Override
    public String ext() {
        return ext;
    }
    public GCEventImpl ext(String ext) {
        this.ext = ext;
        return this;
    }

    @Override
    public Map<Generation, Capacity> capacityByGeneration() {
        return Collections.unmodifiableMap(capacityByGeneration);
    }
    public GCEventImpl capacityByGeneration(Map<Generation, Capacity> capacityByGeneration) {
        this.capacityByGeneration = capacityByGeneration;
        return this;
    }

    public GCEventImpl() {
    }

    public GCEventImpl(GCEvent other) {
        this.id = other.id();
        this.jvmId = other.jvmId();
        this.bucketId = other.bucketId();
        this.parentEvent = other.parentEvent().orElse(null);
        this.analyseId = other.analyseId();
        this.description = other.description();
        this.occurred = other.occurred();
        this.timestamp = other.timestamp();
        this.vmEventType = other.vmEventType();
        this.phase = other.phase();
        this.cause = other.cause();
        this.properties = other.properties();
        this.capacity = other.capacity();
        this.totalCapacity = other.totalCapacity();
        this.pauseMu = other.pauseMu();
        this.generations = other.generations();
        this.concurrency = other.concurrency();
        this.capacityByGeneration = other.capacityByGeneration();
        this.ext = other.ext();
    }

    protected String id;
    protected String jvmId;
    protected String bucketId;
    protected String parentEvent;
    protected String analyseId;
    protected String description;
    protected DateTime occurred;
    protected double timestamp;
    protected VMEventType vmEventType;
    protected Phase phase;
    protected Cause cause;
    protected long properties;
    protected Capacity capacity;
    protected Capacity totalCapacity;
    protected long pauseMu;
    protected EnumSet<Generation> generations;
    protected EventConcurrency concurrency;
    protected String ext;
    protected Map<Generation, Capacity> capacityByGeneration;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GCEventImpl gcEvent = (GCEventImpl) o;

        if (Double.compare(gcEvent.timestamp, timestamp) != 0) return false;
        if (properties != gcEvent.properties) return false;
        if (pauseMu != gcEvent.pauseMu) return false;
        if (id != null ? !id.equals(gcEvent.id) : gcEvent.id != null) return false;
        if (jvmId != null ? !jvmId.equals(gcEvent.jvmId) : gcEvent.jvmId != null) return false;
        if (bucketId != null ? !bucketId.equals(gcEvent.bucketId) : gcEvent.bucketId != null) return false;
        if (parentEvent != null ? !parentEvent.equals(gcEvent.parentEvent) : gcEvent.parentEvent != null) return false;
        if (analyseId != null ? !analyseId.equals(gcEvent.analyseId) : gcEvent.analyseId != null) return false;
        if (description != null ? !description.equals(gcEvent.description) : gcEvent.description != null) return false;
        if (occurred != null ? !occurred.equals(gcEvent.occurred) : gcEvent.occurred != null) return false;
        if (vmEventType != gcEvent.vmEventType) return false;
        if (phase != gcEvent.phase) return false;
        if (cause != gcEvent.cause) return false;
        if (capacity != null ? !capacity.equals(gcEvent.capacity) : gcEvent.capacity != null) return false;
        if (totalCapacity != null ? !totalCapacity.equals(gcEvent.totalCapacity) : gcEvent.totalCapacity != null)
            return false;
        if (generations != null ? !generations.equals(gcEvent.generations) : gcEvent.generations != null) return false;
        if (concurrency != gcEvent.concurrency) return false;
        if (ext != null ? !ext.equals(gcEvent.ext) : gcEvent.ext != null) return false;
        return capacityByGeneration != null ? capacityByGeneration.equals(gcEvent.capacityByGeneration) : gcEvent.capacityByGeneration == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id != null ? id.hashCode() : 0;
        result = 31 * result + (jvmId != null ? jvmId.hashCode() : 0);
        result = 31 * result + (bucketId != null ? bucketId.hashCode() : 0);
        result = 31 * result + (parentEvent != null ? parentEvent.hashCode() : 0);
        result = 31 * result + (analyseId != null ? analyseId.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (occurred != null ? occurred.hashCode() : 0);
        temp = Double.doubleToLongBits(timestamp);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (vmEventType != null ? vmEventType.hashCode() : 0);
        result = 31 * result + (phase != null ? phase.hashCode() : 0);
        result = 31 * result + (cause != null ? cause.hashCode() : 0);
        result = 31 * result + (int) (properties ^ (properties >>> 32));
        result = 31 * result + (capacity != null ? capacity.hashCode() : 0);
        result = 31 * result + (totalCapacity != null ? totalCapacity.hashCode() : 0);
        result = 31 * result + (int) (pauseMu ^ (pauseMu >>> 32));
        result = 31 * result + (generations != null ? generations.hashCode() : 0);
        result = 31 * result + (concurrency != null ? concurrency.hashCode() : 0);
        result = 31 * result + (ext != null ? ext.hashCode() : 0);
        result = 31 * result + (capacityByGeneration != null ? capacityByGeneration.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("GCEventImpl{");
        sb.append("id='").append(id).append('\'');
        sb.append(", jvmId='").append(jvmId).append('\'');
        sb.append(", bucketId='").append(bucketId).append('\'');
        sb.append(", parentEvent='").append(parentEvent).append('\'');
        sb.append(", analyseId='").append(analyseId).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", occurred=").append(occurred);
        sb.append(", timestamp=").append(timestamp);
        sb.append(", vmEventType=").append(vmEventType);
        sb.append(", phase=").append(phase);
        sb.append(", cause=").append(cause);
        sb.append(", properties=").append(properties);
        sb.append(", capacity=").append(capacity);
        sb.append(", totalCapacity=").append(totalCapacity);
        sb.append(", pauseMu=").append(pauseMu);
        sb.append(", generations=").append(generations);
        sb.append(", concurrency=").append(concurrency);
        sb.append(", ext='").append(ext).append('\'');
        sb.append(", capacityByGeneration=").append(capacityByGeneration);
        sb.append('}');
        return sb.toString();
    }
}
