package com.gcplot.repository;

import com.gcplot.model.gc.GCEvent;
import com.gcplot.utils.Range;
import org.joda.time.DateTime;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public interface GCEventRepository extends VMEventsRepository<GCEvent> {

    Optional<GCEvent> lastEvent(String analyseId, String jvmId, String bucketId, DateTime start);

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
