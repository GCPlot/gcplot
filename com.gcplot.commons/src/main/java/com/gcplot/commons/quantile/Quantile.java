package com.gcplot.commons.quantile;

/**
 *
 */
public class Quantile {
    public final double quantile;
    public final double error;
    public final double u;
    public final double v;

    public Quantile(double quantile, double error) {
        this.quantile = quantile;
        this.error = error;
        this.u = 2.0 * error / (1.0 - quantile);
        this.v = 2.0 * error / quantile;
    }

    @Override
    public String toString() {
        return String.format("Q{q=%.3f, eps=%.3f})", quantile, error);
    }
}