package com.gcplot.interceptors;

import com.gcplot.model.Property;
import com.gcplot.model.gc.GCEvent;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         11/20/16
 */
public class BaseInterceptor {
    protected GCEvent ratePreviousEvent;
    protected long allocatedSum;
    protected long promotedSum;
    protected long allocationRateSum;
    protected long allocationRateCount;
    protected long promotionRateSum;
    protected long promotionRateCount;

    protected void countRates(GCEvent event) {
        if (ratePreviousEvent != null) {
            long period = Math.abs(ratePreviousEvent.occurred().getMillis() - event.occurred().getMillis());
            long allocated = Math.abs(ratePreviousEvent.capacity().usedBefore() - event.capacity().usedAfter());
            allocatedSum += allocated;
            allocationRateSum += ((1000 * allocated) / period);
            allocationRateCount++;

            long youngDecreased = Math.abs(event.capacity().usedBefore() - event.capacity().usedAfter());
            long totalDecreased = Math.abs(event.totalCapacity().usedBefore() - event.totalCapacity().usedAfter());
            // it's not a promotion when TOTAL heap decreased more than YOUNG
            if (!event.hasProperty(Property.G1_MIXED) && totalDecreased < youngDecreased) {
                long promoted = Math.abs(totalDecreased - youngDecreased);
                promotedSum += promoted;
                promotionRateSum += ((1000 * promoted) / period);
                promotionRateCount++;
            }
        }
        ratePreviousEvent = event;
    }
}
