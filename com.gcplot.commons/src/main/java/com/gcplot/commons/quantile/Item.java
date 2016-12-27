package com.gcplot.commons.quantile;

/**
 * https://github.com/umbrant/QuantileEstimation
 */
public class Item {
    public final long value;
    public int g;
    public final int delta;

    public Item(long value, int lower_delta, int delta) {
        this.value = value;
        this.g = lower_delta;
        this.delta = delta;
    }

    @Override
    public String toString() {
        return String.format("%d, %d, %d", value, g, delta);
    }
}
