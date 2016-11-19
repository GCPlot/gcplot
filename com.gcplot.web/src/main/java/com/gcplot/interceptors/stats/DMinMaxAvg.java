package com.gcplot.interceptors.stats;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.math.DoubleMath;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         11/19/16
 */
public class DMinMaxAvg {
    private double min = -1;
    private double max = -1;
    private double sum = 0;
    private double count = 0;

    @JsonProperty("min")
    public double getMin() {
        return Math.max(min, 0);
    }

    @JsonProperty("max")
    public double getMax() {
        return Math.max(max, 0);
    }

    @JsonProperty("avg")
    public double getAvg() {
        return Math.max(sum, 1) / Math.max(count, 1);
    }

    public void next(double val) {
        if (val > 0) {
            if (DoubleMath.fuzzyEquals(min, -1d, 0.01) || val < min) {
                min = val;
            }
            if (DoubleMath.fuzzyEquals(min, -1d, 0.01) || val > max) {
                max = val;
            }
            sum += val;
            count++;
        }
    }
}
