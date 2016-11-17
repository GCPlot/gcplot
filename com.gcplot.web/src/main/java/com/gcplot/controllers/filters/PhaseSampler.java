package com.gcplot.controllers.filters;

import com.gcplot.model.gc.GCEvent;
import com.gcplot.model.gc.Phase;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         11/16/16
 */
public class PhaseSampler extends Sampler {
    private Predicate<GCEvent> filter;
    private Map<Phase, EventsBundle> events = new HashMap<>();

    public PhaseSampler(int sampleSeconds, Predicate<GCEvent> filter) {
        super(sampleSeconds, null);
        this.filter = filter;
    }

    @Override
    public boolean isApplicable(GCEvent event) {
        return filter.test(event);
    }

    @Override
    public void process(GCEvent event, Consumer<GCEvent> write) {
       process(event, write, events.computeIfAbsent(event.phase(), k -> new EventsBundle()));
    }

    @Override
    public void complete(Consumer<GCEvent> write) {
        events.forEach((p, b) -> complete(write, b));
    }
}
