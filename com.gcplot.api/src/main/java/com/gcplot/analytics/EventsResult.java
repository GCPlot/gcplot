package com.gcplot.analytics;

import com.google.common.base.Strings;

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
