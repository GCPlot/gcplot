package com.gcplot.services.analytics;

import com.gcplot.commons.interceptors.EventInterceptor;
import com.gcplot.model.IdentifiedEvent;
import com.gcplot.model.gc.GCEvent;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class EventsAnalyticsProcessor {

    public void processEvents(Consumer<IdentifiedEvent> listener, int sampleSeconds, Iterator<GCEvent> i,
                              List<EventInterceptor<GCEvent>> samplers, List<EventInterceptor> interceptors) {
        while (i.hasNext()) {
            GCEvent next = i.next();

            if (sampleSeconds > 1) {
                boolean wasAccepted = false;
                for (EventInterceptor<GCEvent> sampler : samplers) {
                    if (sampler.isApplicable(next)) {
                        sampler.process(next).forEach(listener);
                        wasAccepted = true;
                        break;
                    }
                }
                if (!wasAccepted) {
                    listener.accept(next);
                }
            } else {
                listener.accept(next);
            }
            interceptors.forEach(ic -> ic.process(next).forEach(listener));
        }
        if (sampleSeconds > 1) {
            samplers.forEach(s -> s.complete().forEach(listener));
        }
        interceptors.forEach(ic -> ic.complete().forEach(listener));
    }

}
