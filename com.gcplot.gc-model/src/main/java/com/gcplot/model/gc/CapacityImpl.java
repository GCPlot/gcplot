package com.gcplot.model.gc;

public class CapacityImpl implements Capacity {

    @Override
    public long usedBefore() {
        return usedBefore;
    }

    @Override
    public long usedAfter() {
        return usedAfter;
    }

    @Override
    public long total() {
        return total;
    }

    protected long usedBefore;
    protected long usedAfter;
    protected long total;

    public CapacityImpl(long usedBefore, long usedAfter, long total) {
        this.usedBefore = usedBefore;
        this.usedAfter = usedAfter;
        this.total = total;
    }

    public CapacityImpl() {
    }

}
