package com.gcplot.commons;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class Range {

    public final DateTime from;
    public final DateTime to;

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

}
