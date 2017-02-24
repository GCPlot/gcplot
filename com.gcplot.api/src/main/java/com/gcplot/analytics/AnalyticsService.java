package com.gcplot.analytics;

import com.gcplot.Identifier;
import com.gcplot.model.IdentifiedEvent;
import org.joda.time.Interval;

import java.util.EnumSet;
import java.util.function.Consumer;

public interface AnalyticsService {

    EventsResult events(Identifier accountId, String analyseId, String jvmId, Interval interval,
                        EnumSet<GCEventFeature> features, Consumer<IdentifiedEvent> listener);

    default EventsResult events(Identifier accountId, String analyseId, String jvmId, Interval interval,
                                Consumer<IdentifiedEvent> listener) {
        return events(accountId, analyseId, jvmId, interval, EnumSet.allOf(GCEventFeature.class), listener);
    }

}
