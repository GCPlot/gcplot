package com.gcplot.model.gc;

public class MemoryDetailsImpl implements MemoryDetails {

    @Override
    public long pageSize() {
        return pageSize;
    }
    public MemoryDetailsImpl pageSize(long pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    @Override
    public long physicalTotal() {
        return physicalTotal;
    }
    public MemoryDetailsImpl physicalTotal(long physicalTotal) {
        this.physicalTotal = physicalTotal;
        return this;
    }

    @Override
    public long physicalFree() {
        return physicalFree;
    }
    public MemoryDetailsImpl physicalFree(long physicalFree) {
        this.physicalFree = physicalFree;
        return this;
    }

    @Override
    public long swapTotal() {
        return swapTotal;
    }
    public MemoryDetailsImpl swapTotal(long swapTotal) {
        this.swapTotal = swapTotal;
        return this;
    }

    @Override
    public long swapFree() {
        return swapFree;
    }
    public MemoryDetailsImpl swapFree(long swapFree) {
        this.swapFree = swapFree;
        return this;
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
