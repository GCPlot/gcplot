package com.gcplot.controllers.filters;

import com.gcplot.model.gc.GCEvent;

import java.util.function.Consumer;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         11/13/16
 */
public interface Filter {

    boolean isApplicable(GCEvent event);

    void process(GCEvent event, Consumer<GCEvent> write);

    void complete(Consumer<GCEvent> write);

}
