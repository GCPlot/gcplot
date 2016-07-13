package com.gcplot.repository;

import com.gcplot.commons.Range;
import com.gcplot.model.gc.GCEvent;

import java.util.Iterator;
import java.util.List;

public interface GCEventRepository {

    /**
     * Full events which are stored for the given {@link com.gcplot.model.gc.GCAnalyse} and
     * JVM ID.
     *
     * @param analyseId
     * @param jvmId
     * @param range
     * @return
     */
    List<GCEvent> events(String analyseId, String jvmId, Range range);

    Iterator<GCEvent> lazyEvents(String analyseId, String jvmId, Range range);

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

    void erase(String analyseId, String jvmId, Range range);

    void erase(String analyseId, List<String> jvmId, Range range);

    void add(GCEvent event);

    void add(List<GCEvent> events);

    void addAsync(GCEvent event);

    void addAsync(List<GCEvent> events);

}
