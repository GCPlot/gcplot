package com.gcplot.commons;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

public class Range {
    private final DateTime from;
    private final DateTime to;

    public DateTime from() {
        return from;
    }

    public DateTime to() {
        return to;
    }

    public Range(DateTime from, DateTime to) {
        this(from, to, DateTimeZone.UTC);
    }

    private Range(DateTime from, DateTime to, DateTimeZone timeZone) {
        this.from = new DateTime(from, timeZone);
        this.to = new DateTime(to, timeZone);
    }

    public static Range of(DateTime from, DateTime to) {
        return new Range(from ,to);
    }

    public static Range of(Interval interval) {
        return new Range(interval.getStart(), interval.getEnd());
    }

}
