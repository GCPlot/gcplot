package com.gcplot.repository;

import com.gcplot.commons.Range;
import com.gcplot.model.gc.GCEvent;

import java.util.Iterator;
import java.util.List;

public interface GCEventRepository extends JVMEventsRepository<GCEvent> {

    /**
     * Shortened events which contains only basic info about pauses, GC types, etc.
     *
     * @param analyseId
     * @param jvmId
     * @param range
     * @return
     */
    List<GCEvent> pauseEvents(String analyseId, String jvmId, Range range);

    Iterator<GCEvent> lazyPauseEvents(String analyseId, String jvmId, Range range);

}
