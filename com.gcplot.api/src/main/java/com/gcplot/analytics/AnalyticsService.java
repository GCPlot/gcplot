package com.gcplot.analytics;

import com.gcplot.Identifier;
import com.gcplot.model.IdentifiedEvent;
import org.joda.time.Interval;

import java.util.EnumSet;
import java.util.function.Consumer;

public interface AnalyticsService {

    EventsResult events(Identifier accountId, String analyseId, String jvmId, Interval interval, int samplingSeconds,
                        EnumSet<GCEventFeature> features, Consumer<IdentifiedEvent> listener);

    default EventsResult events(Identifier accountId, String analyseId, String jvmId, Interval interval, int samplingSeconds,
                                Consumer<IdentifiedEvent> listener) {
        return events(accountId, analyseId, jvmId, interval, samplingSeconds, EnumSet.allOf(GCEventFeature.class), listener);
    }

    default EventsResult events(Identifier accountId, String analyseId, String jvmId, Interval interval,
                                EnumSet<GCEventFeature> features, Consumer<IdentifiedEvent> listener) {
        return events(accountId, analyseId, jvmId, interval, 0, features, listener);
    }

    default EventsResult events(Identifier accountId, String analyseId, String jvmId, Interval interval,
                                Consumer<IdentifiedEvent> listener) {
        return events(accountId, analyseId, jvmId, interval, 0, EnumSet.allOf(GCEventFeature.class), listener);
    }

}
