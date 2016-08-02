package com.gcplot.log_processor.parser;

import com.gcplot.log_processor.parser.notifications.GCNotification;
import com.gcplot.log_processor.parser.survivor.AgesState;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/30/16
 */
public class ParseResult {
    private final boolean isSuccessful;
    public boolean isSuccessful() {
        return isSuccessful;
    }

    private final Throwable exception;
    public Throwable getException() {
        return exception;
    }

    private final List<GCNotification> notifications;
    public List<GCNotification> getNotifications() {
        return Collections.unmodifiableList(notifications);
    }

    private final List<AgesState> agesStates;
    public List<AgesState> getAgesStates() {
        return agesStates;
    }

    private ParseResult(List<GCNotification> notifications, List<AgesState> agesState) {
        this(true, null, notifications, agesState);
    }

    private ParseResult(boolean isSuccessful, Throwable exception,
                        List<GCNotification> notifications, List<AgesState> agesStates) {
        this.isSuccessful = isSuccessful;
        this.notifications = notifications;
        this.agesStates = agesStates;
        this.exception = exception;
    }

    public static ParseResult success(List<GCNotification> notifications, List<AgesState> agesStates) {
        return new ParseResult(notifications, agesStates);
    }

    public static ParseResult failure(Throwable t) {
        return new ParseResult(false, t, Collections.emptyList(), Collections.emptyList());
    }

}
