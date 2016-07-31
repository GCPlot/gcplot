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

    private final AgesState agesState;
    public AgesState getAgesState() {
        return agesState;
    }

    private ParseResult(List<GCNotification> notifications, AgesState agesState) {
        this(true, null, notifications, agesState);
    }

    private ParseResult(boolean isSuccessful, Throwable exception,
                        List<GCNotification> notifications, AgesState agesState) {
        this.isSuccessful = isSuccessful;
        this.notifications = notifications;
        this.agesState = agesState;
        this.exception = exception;
    }

    public static ParseResult success(List<GCNotification> notifications, AgesState agesState) {
        return new ParseResult(notifications, agesState);
    }

    public static ParseResult failure(Throwable t) {
        return new ParseResult(false, t, Collections.emptyList(), AgesState.NONE);
    }

}
