package com.gcplot.services.filters;

import com.gcplot.model.gc.Capacity;
import com.gcplot.model.gc.GCEvent;
import com.gcplot.model.gc.GCEventFactory;
import com.gcplot.model.gc.Phase;
import com.gcplot.services.EventInterceptor;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         11/13/16
 */
public class Accumulator implements EventInterceptor<GCEvent> {
    private final int windowSeconds;
    private final Predicate<GCEvent> eventChooser;
    private final GCEventFactory eventFactory;
    private List<GCEvent> tenured = new ArrayList<>();
    private DateTime edge = null;
    private DateTime edgeMinus = null;

    public Accumulator(int windowSeconds, GCEventFactory eventFactory,
                       Predicate<GCEvent> eventChooser) {
        this.windowSeconds = windowSeconds;
        this.eventChooser = eventChooser;
        this.eventFactory = eventFactory;
    }

    @Override
    public boolean isApplicable(GCEvent event) {
        return eventChooser.test(event);
    }

    @Override
    public List<GCEvent> process(GCEvent event) {
        if (edge == null) {
            edge(event);
        }
        if (edgeMinus.isBefore(event.occurred()) || edge == event.occurred()) {
            tenured.add(event);
            return Collections.emptyList();
        } else {
            Capacity capacity = Capacity.NONE;
            Capacity totalCapacity = Capacity.NONE;
            long pauseSum = 0;
            GCEvent newEvent = null;
            for (GCEvent e : tenured) {
                pauseSum += e.pauseMu();
                if (capacity.equals(Capacity.NONE) && !e.capacity().equals(Capacity.NONE)) {
                    capacity = e.capacity();
                    totalCapacity = e.totalCapacity();
                }
                // still might be NONE
                if (totalCapacity.equals(Capacity.NONE) && !e.capacity().equals(Capacity.NONE)) {
                    totalCapacity = e.totalCapacity();
                }
            }
            if (tenured.size() > 0) {
                newEvent = eventFactory.create(tenured.get(0), capacity, totalCapacity, pauseSum, Phase.OTHER);
                tenured.clear();
            }

            edge(event);
            tenured.add(event);

            return newEvent == null ? Collections.emptyList() : Collections.singletonList(newEvent);
        }
    }

    @Override
    public List<GCEvent> complete() {
        List<GCEvent> events = new ArrayList<>(tenured);
        tenured.clear();
        return events;
    }

    private void edge(GCEvent event) {
        edge = event.occurred();
        edgeMinus = edge.minusSeconds(windowSeconds);
    }
}
