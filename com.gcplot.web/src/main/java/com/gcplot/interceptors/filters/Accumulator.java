package com.gcplot.interceptors.filters;

import com.gcplot.model.gc.Capacity;
import com.gcplot.model.gc.GCEvent;
import com.gcplot.model.gc.GCEventImpl;
import com.gcplot.model.gc.Phase;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         11/13/16
 */
public class Accumulator implements Filter {
    private final int windowSeconds;
    private final Predicate<GCEvent> eventChooser;
    private List<GCEvent> tenured = new ArrayList<>();
    private DateTime edge = null;
    private DateTime edgeMinus = null;

    public Accumulator(int windowSeconds, Predicate<GCEvent> eventChooser) {
        this.windowSeconds = windowSeconds;
        this.eventChooser = eventChooser;
    }

    @Override
    public boolean isApplicable(GCEvent event) {
        return eventChooser.test(event);
    }

    @Override
    public void process(GCEvent event, Consumer<GCEvent> write) {
        if (edge == null) {
            edge(event);
        }
        if (edgeMinus.isBefore(event.occurred()) || edge == event.occurred()) {
            tenured.add(event);
        } else {
            Capacity capacity = Capacity.NONE;
            Capacity totalCapacity = Capacity.NONE;
            long pauseSum = 0;
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
                GCEventImpl e = new GCEventImpl(tenured.get(0));
                e.capacity(capacity);
                e.totalCapacity(totalCapacity);
                e.pauseMu(pauseSum);
                e.phase(Phase.OTHER);

                write.accept(e);
                tenured.clear();
            }

            edge(event);
            tenured.add(event);
        }
    }

    @Override
    public void complete(Consumer<GCEvent> write) {
        tenured.forEach(write);
        tenured.clear();
    }

    private void edge(GCEvent event) {
        edge = event.occurred();
        edgeMinus = edge.minusSeconds(windowSeconds);
    }
}
