package com.gcplot.analytics;

import com.gcplot.model.DatedEvent;
import com.gcplot.model.IdentifiedEvent;
import com.gcplot.model.gc.GCEvent;
import com.gcplot.model.stats.GCStatistic;
import com.google.common.base.Strings;
import com.sun.xml.internal.xsom.impl.scd.Iterators;

import java.util.Iterator;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         2/24/17
 */
public class EventsResult {
    public static final EventsResult SUCCESS = new EventsResult();
    private final String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isSuccess() {
        return Strings.isNullOrEmpty(errorMessage);
    }

    public EventsResult() {
        this.errorMessage = null;
    }

    public EventsResult(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
