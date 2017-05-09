package com.gcplot.services.filters;

import com.gcplot.model.gc.GCEvent;
import com.gcplot.model.gc.Phase;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         11/16/16
 */
public class PhaseSampler extends Sampler {
    private Predicate<GCEvent> filter;
    private Map<Phase, EventsBundle> events = new IdentityHashMap<>();

    public PhaseSampler(int sampleSeconds, Predicate<GCEvent> filter) {
        super(sampleSeconds, null);
        this.filter = filter;
    }

    @Override
    public boolean isApplicable(GCEvent event) {
        return filter.test(event);
    }

    @Override
    public List<GCEvent> process(GCEvent event) {
       return process(event, events.computeIfAbsent(event.phase(), k -> new EventsBundle()));
    }

    @Override
    public List<GCEvent> complete() {
        List<GCEvent> e = new ArrayList<>();
        events.forEach((p, b) -> e.addAll(complete(b)));
        return e;
    }

    @Override
    protected List<GCEvent> writeAndReset(EventsBundle b) {
        List<GCEvent> e = new ArrayList<>();
        events.forEach((p, bb) -> e.addAll(super.writeAndReset(bb)));
        return e;
    }
}
