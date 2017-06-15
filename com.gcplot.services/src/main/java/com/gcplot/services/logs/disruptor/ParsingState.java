package com.gcplot.services.logs.disruptor;

import com.gcplot.commons.LazyVal;
import com.gcplot.model.gc.GCEvent;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         6/14/17
 */
public class ParsingState {
    private final LazyVal<GCEvent> lastPersistedEvent;
    private GCEvent firstEvent;
    private GCEvent lastEvent;

    public ParsingState(LazyVal<GCEvent> lastPersistedEvent) {
        this.lastPersistedEvent = lastPersistedEvent;
    }

    public LazyVal<GCEvent> getLastPersistedEvent() {
        return lastPersistedEvent;
    }

    public GCEvent getFirstEvent() {
        return firstEvent;
    }

    public void setFirstEvent(GCEvent firstEvent) {
        this.firstEvent = firstEvent;
    }

    public GCEvent getLastEvent() {
        return lastEvent;
    }

    public void setLastEvent(GCEvent lastEvent) {
        this.lastEvent = lastEvent;
    }
}
