package com.gcplot.controllers.filters;

import com.gcplot.model.gc.GCEvent;
import com.gcplot.model.gc.Generation;
import org.joda.time.DateTime;

import java.util.EnumSet;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         11/13/16
 */
public class Sampler implements Filter {
    private final EnumSet<Generation> applyTo;
    private final int sampleSeconds;
    private GCEvent min = null, max = null, rest = null;
    private DateTime edge = null;
    private DateTime edgeMinus = null;

    public Sampler(int sampleSeconds) {
        this(sampleSeconds, EnumSet.noneOf(Generation.class));
    }

    public Sampler(int sampleSeconds, EnumSet<Generation> applyTo) {
        this.sampleSeconds = sampleSeconds;
        this.applyTo = applyTo;
    }

    @Override
    public boolean isApplicable(GCEvent event) {
        return applyTo.size() <= 0 || event.generations().equals(applyTo);
    }

    @Override
    public void process(GCEvent event, Consumer<GCEvent> write) {
        if (min == null) {
            min = event;
        }
        if (max == null) {
            max = event;
        }
        if (edge == null) {
            edge(event);
        }
        if (edgeMinus.isBefore(event.occurred())) {
            if (event.pauseMu() < min.pauseMu()) {
                min = event;
            } else if (event.pauseMu() > max.pauseMu()) {
                max = event;
            } else if (rest == null) {
                rest = event;
            }
        } else {
            if (!min.equals(max)) {
                write.accept(min);
                write.accept(max);
            } else {
                write.accept(min);
            }
            if (rest != null && (!(min.equals(rest) || max.equals(rest)))) {
                write.accept(rest);
            }
            min = null;
            max = null;
            rest = null;
            edge(event);
        }
    }

    @Override
    public void complete(Consumer<GCEvent> write) {
        if (min != null) {
            write.accept(min);
        }
        if (max != null) {
            write.accept(max);
        }
        if (rest != null) {
            write.accept(rest);
        }
    }

    private void edge(GCEvent event) {
        edge = event.occurred();
        edgeMinus = edge.minusSeconds(sampleSeconds);
    }
}
