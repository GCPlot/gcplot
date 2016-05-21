package com.gcplot.model.gc;

public class CapacityImpl implements Capacity {

    @Override
    public long usedBefore() {
        return usedBefore;
    }
    public void usedBefore(long usedBefore) {
        this.usedBefore = usedBefore;
    }

    @Override
    public long usedAfter() {
        return usedAfter;
    }
    public void usedAfter(long usedAfter) {
        this.usedAfter = usedAfter;
    }

    @Override
    public long total() {
        return total;
    }
    public void total(long total) {
        this.total = total;
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
