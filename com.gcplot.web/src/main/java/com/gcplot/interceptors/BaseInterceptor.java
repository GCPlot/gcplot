package com.gcplot.interceptors;

import com.gcplot.model.gc.GCEvent;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         11/20/16
 */
public class BaseInterceptor {
    protected GCEvent ratePreviousEvent;
    protected long allocationRateSum;
    protected long allocationRateCount;
    protected long promotionRateSum;
    protected long promotionRateCount;

    protected void countRates(GCEvent event) {
        if (ratePreviousEvent != null) {
            long period = Math.abs(ratePreviousEvent.occurred().getMillis() - event.occurred().getMillis());
            long allocated = Math.abs(ratePreviousEvent.capacity().usedBefore() - event.capacity().usedAfter());
            allocationRateSum += 1000 * allocated / period;
            allocationRateCount++;

            long youngDecreased = Math.abs(event.capacity().usedBefore() - event.capacity().usedAfter());
            long totalDecreased = Math.abs(event.totalCapacity().usedBefore() - event.totalCapacity().usedAfter());
            long promoted = Math.abs(totalDecreased - youngDecreased);
            promotionRateSum += 1000 * promoted / period;
            promotionRateCount++;
        }
        ratePreviousEvent = event;
    }
}
