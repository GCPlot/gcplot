package com.gcplot.model.gc;

import com.google.common.base.MoreObjects;
import org.joda.time.DateTime;

import java.util.EnumSet;
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

    protected String id;
    protected String jvmId;
    protected String parentEvent;
    protected String analyseId;
    protected String description;
    protected DateTime occurred;
    protected double timestamp;
    protected VMEventType vmEventType;
    protected Capacity capacity;
    protected Capacity totalCapacity;
    protected long pauseMu;
    protected EnumSet<Generation> generations;
    protected EventConcurrency concurrency;
    protected String ext;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GCEventImpl gcEvent = (GCEventImpl) o;

        if (Double.compare(gcEvent.timestamp, timestamp) != 0) return false;
        if (pauseMu != gcEvent.pauseMu) return false;
        if (id != null ? !id.equals(gcEvent.id) : gcEvent.id != null) return false;
        if (jvmId != null ? !jvmId.equals(gcEvent.jvmId) : gcEvent.jvmId != null) return false;
        if (parentEvent != null ? !parentEvent.equals(gcEvent.parentEvent) : gcEvent.parentEvent != null) return false;
        if (analyseId != null ? !analyseId.equals(gcEvent.analyseId) : gcEvent.analyseId != null) return false;
        if (description != null ? !description.equals(gcEvent.description) : gcEvent.description != null) return false;
        if (occurred != null ? !occurred.equals(gcEvent.occurred) : gcEvent.occurred != null) return false;
        if (vmEventType != gcEvent.vmEventType) return false;
        if (capacity != null ? !capacity.equals(gcEvent.capacity) : gcEvent.capacity != null) return false;
        if (totalCapacity != null ? !totalCapacity.equals(gcEvent.totalCapacity) : gcEvent.totalCapacity != null)
            return false;
        if (generations != null ? !generations.equals(gcEvent.generations) : gcEvent.generations != null) return false;
        if (concurrency != gcEvent.concurrency) return false;
        return ext != null ? ext.equals(gcEvent.ext) : gcEvent.ext == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id != null ? id.hashCode() : 0;
        result = 31 * result + (jvmId != null ? jvmId.hashCode() : 0);
        result = 31 * result + (parentEvent != null ? parentEvent.hashCode() : 0);
        result = 31 * result + (analyseId != null ? analyseId.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (occurred != null ? occurred.hashCode() : 0);
        temp = Double.doubleToLongBits(timestamp);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (vmEventType != null ? vmEventType.hashCode() : 0);
        result = 31 * result + (capacity != null ? capacity.hashCode() : 0);
        result = 31 * result + (totalCapacity != null ? totalCapacity.hashCode() : 0);
        result = 31 * result + (int) (pauseMu ^ (pauseMu >>> 32));
        result = 31 * result + (generations != null ? generations.hashCode() : 0);
        result = 31 * result + (concurrency != null ? concurrency.hashCode() : 0);
        result = 31 * result + (ext != null ? ext.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("jvmId", jvmId)
                .add("parentEvent", parentEvent)
                .add("analyseId", analyseId)
                .add("description", description)
                .add("occurred", occurred)
                .add("timestamp", timestamp)
                .add("vmEventType", vmEventType)
                .add("capacity", capacity)
                .add("totalCapacity", totalCapacity)
                .add("pauseMu", pauseMu)
                .add("generations", generations)
                .add("concurrency", concurrency)
                .add("ext", ext)
                .toString();
    }
}
