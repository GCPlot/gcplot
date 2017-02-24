package com.gcplot.model.stats;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         11/18/16
 */
public class MinMaxAvg {
    private long min = -1;
    private long max = -1;
    private long sum = 0;
    private long count = 0;

    @JsonProperty("min")
    public long getMin() {
        return Math.max(min, 0);
    }

    @JsonProperty("max")
    public long getMax() {
        return Math.max(max, 0);
    }

    @JsonProperty("avg")
    public long getAvg() {
        return Math.max(sum, 1) / Math.max(count, 1);
    }

    public void next(long val) {
        if (min == -1 || val < min) {
            min = val;
        }
        if (max == -1 || val > max) {
            max = val;
        }
        sum += val;
        count++;
    }
}
