package com.gcplot.model.gc;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import java.util.List;

public class Capacity {
    public static final Capacity NONE = new Capacity();

    public long usedBefore() {
        return usedBefore;
    }

    public long usedAfter() {
        return usedAfter;
    }

    public long total() {
        return total;
    }

    protected final long usedBefore;
    protected final long usedAfter;
    protected final long total;

    public Capacity(List<Long> list) {
        Preconditions.checkState(list.size() == 3, "Wrong input list: %s", list);
        this.usedBefore = list.get(0);
        this.usedAfter = list.get(1);
        this.total = list.get(2);
    }

    public Capacity(long usedBefore, long usedAfter, long total) {
        this.usedBefore = usedBefore;
        this.usedAfter = usedAfter;
        this.total = total;
    }

    public Capacity(Capacity capacity) {
        this.usedBefore = capacity.usedBefore();
        this.usedAfter = capacity.usedAfter();
        this.total = capacity.total();
    }

    public Capacity() {
        this.usedBefore = 0;
        this.usedAfter = 0;
        this.total = 0;
    }

    public static Capacity of(long usedBefore, long usedAfter, long total) {
        return new Capacity(usedBefore, usedAfter, total);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Capacity capacity = (Capacity) o;

        if (usedBefore != capacity.usedBefore) return false;
        if (usedAfter != capacity.usedAfter) return false;
        return total == capacity.total;

    }

    @Override
    public int hashCode() {
        int result = (int) (usedBefore ^ (usedBefore >>> 32));
        result = 31 * result + (int) (usedAfter ^ (usedAfter >>> 32));
        result = 31 * result + (int) (total ^ (total >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("usedBefore", usedBefore)
                .add("usedAfter", usedAfter)
                .add("total", total)
                .toString();
    }
}
