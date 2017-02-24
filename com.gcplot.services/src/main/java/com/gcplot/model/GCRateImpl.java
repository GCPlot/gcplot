package com.gcplot.model;

import com.gcplot.model.gc.GCRate;
import org.joda.time.DateTime;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         2/24/17
 */
public class GCRateImpl implements GCRate {
    private final DateTime occurred;
    private final long allocationRate;
    private final long promotionRate;

    @Override
    public DateTime occurred() {
        return occurred;
    }

    @Override
    public long allocationRate() {
        return allocationRate;
    }

    @Override
    public long promotionRate() {
        return promotionRate;
    }

    public GCRateImpl(DateTime occurred, long allocationRate, long promotionRate) {
        this.occurred = occurred;
        this.allocationRate = allocationRate;
        this.promotionRate = promotionRate;
    }
}
