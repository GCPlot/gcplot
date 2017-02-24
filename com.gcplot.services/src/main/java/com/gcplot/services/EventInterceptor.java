package com.gcplot.services;

import com.gcplot.model.DatedEvent;
import com.gcplot.model.IdentifiedEvent;
import com.gcplot.model.gc.GCEvent;

import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         11/18/16
 */
public interface EventInterceptor<T extends IdentifiedEvent> {

    default boolean isApplicable(GCEvent event) {
        return true;
    }

    List<T> process(GCEvent event);

    List<T> complete();

}
