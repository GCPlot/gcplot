package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.model.gc.MemoryDetails;
import com.google.common.base.MoreObjects;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/28/16
 */
public class MemoryStatus {
    @JsonProperty(value = "ps")
    public long pageSize;
    @JsonProperty(value = "pt")
    public long physicalTotal;
    @JsonProperty(value = "pf")
    public long physicalFree;
    @JsonProperty(value = "st")
    public long swapTotal;
    @JsonProperty(value = "sf")
    public long swapFree;

    public MemoryStatus(@JsonProperty(value = "ps") long pageSize,
                        @JsonProperty(value = "pt") long physicalTotal,
                        @JsonProperty(value = "pf") long physicalFree,
                        @JsonProperty(value = "st") long swapTotal,
                        @JsonProperty(value = "sf") long swapFree) {
        this.pageSize = pageSize;
        this.physicalTotal = physicalTotal;
        this.physicalFree = physicalFree;
        this.swapTotal = swapTotal;
        this.swapFree = swapFree;
    }

    public MemoryStatus(MemoryDetails details) {
        this.pageSize = details.pageSize();
        this.physicalTotal = details.physicalTotal();
        this.physicalFree = details.physicalFree();
        this.swapTotal = details.swapTotal();
        this.swapFree = details.swapFree();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MemoryStatus that = (MemoryStatus) o;

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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("pageSize", pageSize)
                .add("physicalTotal", physicalTotal)
                .add("physicalFree", physicalFree)
                .add("swapTotal", swapTotal)
                .add("swapFree", swapFree)
                .toString();
    }
}
