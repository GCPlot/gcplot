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

    public boolean isEmpty() {
        return pageSize == 0 && physicalTotal == 0 && physicalFree == 0
                && swapTotal == 0 && swapFree == 0;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MemoryDetailsImpl that = (MemoryDetailsImpl) o;

        if (pageSize != that.pageSize) return false;
        if (physicalTotal != that.physicalTotal) return false;
        if (physicalFree != that.physicalFree) return false;
        if (swapTotal != that.swapTotal) return false;
        return swapFree == that.swapFree;

    }

    @Override
    public int hashCode() {
        int result = (int) (pageSize ^ (pageSize >>> 32));
        result = 31 * result + (int) (physicalTotal ^ (physicalTotal >>> 32));
        result = 31 * result + (int) (physicalFree ^ (physicalFree >>> 32));
        result = 31 * result + (int) (swapTotal ^ (swapTotal >>> 32));
        result = 31 * result + (int) (swapFree ^ (swapFree >>> 32));
        return result;
    }
}
