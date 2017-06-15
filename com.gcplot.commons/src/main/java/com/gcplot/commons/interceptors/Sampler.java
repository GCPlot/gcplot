package com.gcplot.commons.interceptors;

import com.gcplot.model.gc.GCEvent;
import com.gcplot.model.gc.GCEvents;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         11/13/16
 */
public class Sampler implements EventInterceptor<GCEvent> {
    private final Predicate<GCEvent> applyTo;
    private final int sampleSeconds;
    private EventsBundle events = new EventsBundle();
    private DateTime edge = null;
    private DateTime edgeMinus = null;

    public Sampler(int sampleSeconds, Predicate<GCEvent> applyTo) {
        this.sampleSeconds = sampleSeconds;
        this.applyTo = applyTo;
    }

    @Override
    public boolean isApplicable(GCEvent event) {
        return applyTo.test(event);
    }

    @Override
    public List<GCEvent> process(GCEvent event) {
        EventsBundle b = events;
        return process(event, b);
    }

    @Override
    public List<GCEvent> complete() {
        EventsBundle b = events;
        return complete(b);
    }

    protected List<GCEvent> process(GCEvent event, EventsBundle b) {
        initialProcess(event, b);
        List<GCEvent> events = Collections.emptyList();
        if (edgeMinus.isBefore(event.occurred())) {
            if (event.pauseMu() < b.getMin().pauseMu()) {
                b.setMin(event);
            } else if (event.pauseMu() > b.getMax().pauseMu()) {
                b.setMax(event);
            } else if (b.getRest() == null || b.getRest() == b.getMin() || b.getRest() == b.getMax()) {
                b.setRest(event);
            }
        } else {
            events = writeAndReset(b);
            initialProcess(event, b);
            edge(event);
        }
        return events;
    }

    private void initialProcess(GCEvent event, EventsBundle b) {
        if (b.getMin() == null) {
            b.setMin(event);
        }
        if (b.getMax() == null) {
            b.setMax(event);
        }
        if (edge == null) {
            edge(event);
        }
    }

    protected List<GCEvent> writeAndReset(EventsBundle b) {
        if (b.isEmpty()) return Collections.emptyList();
        List<GCEvent> events = new ArrayList<>(3);
        if (!GCEvents.lightEquals(b.getMin(), b.getMax())) {
            events.add(b.getMin());
            events.add(b.getMax());
        } else {
            events.add(b.getMin());
        }
        if (b.getRest() != null && (!(GCEvents.lightEquals(b.getMin(), b.getRest())
                || GCEvents.lightEquals(b.getMax(), b.getRest())))) {
            events.add(b.getRest());
        }
        b.setMin(null);
        b.setMax(null);
        b.setRest(null);
        return events;
    }

    protected List<GCEvent> complete(EventsBundle b) {
        List<GCEvent> events = new ArrayList<>(3);
        if (b.getMin() != null) {
            events.add(b.getMin());
        }
        if (b.getMax() != null) {
            events.add(b.getMax());
        }
        if (b.getRest() != null) {
            events.add(b.getRest());
        }
        return events;
    }

    protected void edge(GCEvent event) {
        edge = event.occurred();
        edgeMinus = edge.minusSeconds(sampleSeconds);
    }

}
