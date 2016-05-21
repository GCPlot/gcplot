package com.gcplot.model.gc;

import com.gcplot.Identifier;
import org.joda.time.DateTime;

import java.util.List;

public class GCAnalyseImpl implements GCAnalyse {

    @Override
    public String id() {
        return id;
    }
    public GCAnalyseImpl id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public Identifier accountId() {
        return accountId;
    }
    public GCAnalyseImpl accountId(Identifier accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public String name() {
        return name;
    }
    public GCAnalyseImpl name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public boolean isContinuous() {
        return isContinous;
    }
    public GCAnalyseImpl isContinuous(boolean isContinuous) {
        this.isContinous = isContinuous;
        return this;
    }

    @Override
    public DateTime start() {
        return start;
    }
    public GCAnalyseImpl start(DateTime start) {
        this.start = start;
        return this;
    }

    @Override
    public DateTime lastEvent() {
        return lastEvent;
    }
    public GCAnalyseImpl lastEvent(DateTime lastEvent) {
        this.lastEvent = lastEvent;
        return this;
    }

    @Override
    public GarbageCollectorType collectorType() {
        return collectorType;
    }
    public GCAnalyseImpl collectorType(GarbageCollectorType collectorType) {
        this.collectorType = collectorType;
        return this;
    }

    @Override
    public List<String> commandLineParams() {
        return commandLineParams;
    }
    public GCAnalyseImpl commandLineParams(List<String> commandLineParams) {
        this.commandLineParams = commandLineParams;
        return this;
    }

    @Override
    public String header() {
        return header;
    }
    public GCAnalyseImpl header(String header) {
        this.header = header;
        return this;
    }

    @Override
    public MemoryDetailsImpl memoryDetails() {
        return memoryDetails;
    }
    public GCAnalyseImpl memoryDetails(MemoryDetailsImpl memoryDetails) {
        this.memoryDetails = memoryDetails;
        return this;
    }

    protected String id;
    protected Identifier accountId;
    protected String name;
    protected boolean isContinous;
    protected DateTime start;
    protected DateTime lastEvent;
    protected GarbageCollectorType collectorType;
    protected List<String> commandLineParams;
    protected String header;
    protected MemoryDetailsImpl memoryDetails;

}
