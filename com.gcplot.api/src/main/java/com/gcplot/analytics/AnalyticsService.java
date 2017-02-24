package com.gcplot.analytics;

import com.gcplot.Identifier;
import com.gcplot.model.IdentifiedEvent;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

import java.util.EnumSet;
import java.util.function.Consumer;

public interface AnalyticsService {

    EventsResult events(Identifier accountId, String analyseId, String jvmId, Interval interval,
                        DateTimeZone tz, EnumSet<GCEventFeature> features, Consumer<IdentifiedEvent> listener);

    default EventsResult events(Identifier accountId, String analyseId, String jvmId, Interval interval, DateTimeZone tz,
                                Consumer<IdentifiedEvent> listener) {
        return events(accountId, analyseId, jvmId, interval, tz, EnumSet.allOf(GCEventFeature.class), listener);
    }

}
