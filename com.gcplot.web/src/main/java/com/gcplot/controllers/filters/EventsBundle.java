package com.gcplot.controllers.filters;

import com.gcplot.model.gc.GCEvent;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         11/16/16
 */
public class EventsBundle {
    private GCEvent min;
    private GCEvent max;
    private GCEvent rest;

    public GCEvent getMin() {
        return min;
    }
    public void setMin(GCEvent min) {
        this.min = min;
    }

    public GCEvent getMax() {
        return max;
    }
    public void setMax(GCEvent max) {
        this.max = max;
    }

    public GCEvent getRest() {
        return rest;
    }
    public void setRest(GCEvent rest) {
        this.rest = rest;
    }

    public boolean isEmpty() {
        return min == null && max == null && rest == null;
    }
}
