package com.gcplot.model.gc;

public class MemoryDetailsImpl implements MemoryDetails {

    @Override
    public long pageSize() {
        return pageSize;
    }
    public void pageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public long physicalTotal() {
        return physicalTotal;
    }
    public void physicalTotal(long physicalTotal) {
        this.physicalTotal = physicalTotal;
    }

    @Override
    public long physicalFree() {
        return physicalFree;
    }
    public void physicalFree(long physicalFree) {
        this.physicalFree = physicalFree;
    }

    @Override
    public long swapTotal() {
        return swapTotal;
    }
    public void swapTotal(long swapTotal) {
        this.swapTotal = swapTotal;
    }

    @Override
    public long swapFree() {
        return swapFree;
    }
    public void swapFree(long swapFree) {
        this.swapFree = swapFree;
    }

    protected long pageSize;
    protected long physicalTotal;
    protected long physicalFree;
    protected long swapTotal;
    protected long swapFree;

    public MemoryDetailsImpl(long pageSize, long physicalTotal, long physicalFree,
                         long swapTotal, long swapFree) {
        this.pageSize = pageSize;
        this.physicalTotal = physicalTotal;
        this.physicalFree = physicalFree;
        this.swapTotal = swapTotal;
        this.swapFree = swapFree;
    }

    public MemoryDetailsImpl() {
    }
}
