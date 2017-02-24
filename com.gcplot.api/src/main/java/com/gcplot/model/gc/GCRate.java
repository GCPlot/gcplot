package com.gcplot.model.gc;

import com.gcplot.model.DatedEvent;
import com.gcplot.model.IdentifiedEvent;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         2/24/17
 */
public interface GCRate extends DatedEvent, IdentifiedEvent {

    long allocationRate();

    long promotionRate();

    @Override
    default boolean isRate() {
        return true;
    }
}
