package com.gcplot.interceptors.filters;

import com.gcplot.model.gc.GCEvent;
import com.gcplot.model.gc.Generation;
import org.joda.time.DateTime;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         11/13/16
 */
public class Sampler implements Filter {
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
    public void process(GCEvent event, Consumer<GCEvent> write) {
        EventsBundle b = events;
        process(event, write, b);
    }

    @Override
    public void complete(Consumer<GCEvent> write) {
        EventsBundle b = events;
        complete(write, b);
    }

    protected void process(GCEvent event, Consumer<GCEvent> write, EventsBundle b) {
        initialProcess(event, b);
        if (edgeMinus.isBefore(event.occurred())) {
            if (event.pauseMu() < b.getMin().pauseMu()) {
                b.setMin(event);
            } else if (event.pauseMu() > b.getMax().pauseMu()) {
                b.setMax(event);
            } else if (b.getRest() == null) {
                b.setRest(event);
            }
        } else {
            writeAndReset(write, b);
            initialProcess(event, b);
            edge(event);
        }
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

    protected void writeAndReset(Consumer<GCEvent> write, EventsBundle b) {
        if (b.isEmpty()) return;
        if (!b.getMin().equals(b.getMax())) {
            write.accept(b.getMin());
            write.accept(b.getMax());
        } else {
            write.accept(b.getMin());
        }
        if (b.getRest() != null && (!(b.getMin().equals(b.getRest())
                || b.getMax().equals(b.getRest())))) {
            write.accept(b.getRest());
        }
        b.setMin(null);
        b.setMax(null);
        b.setRest(null);
    }

    protected void complete(Consumer<GCEvent> write, EventsBundle b) {
        if (b.getMin() != null) {
            write.accept(b.getMin());
        }
        if (b.getMax() != null) {
            write.accept(b.getMax());
        }
        if (b.getRest() != null) {
            write.accept(b.getRest());
        }
    }

    protected void edge(GCEvent event) {
        edge = event.occurred();
        edgeMinus = edge.minusSeconds(sampleSeconds);
    }

}
