package com.gcplot.model.gc;

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
    public long pauseMu() {
        return pauseMu;
    }
    public GCEventImpl pauseMu(long pauseMu) {
        this.pauseMu = pauseMu;
        return this;
    }

    @Override
    public long durationMu() {
        return durationMu;
    }
    public GCEventImpl durationMu(long durationMu) {
        this.durationMu = durationMu;
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
    protected VMEventType vmEventType;
    protected Capacity capacity;
    protected Capacity totalCapacity;
    protected long pauseMu;
    protected long durationMu;
    protected EnumSet<Generation> generations;
    protected EventConcurrency concurrency;
    protected String ext;

}
