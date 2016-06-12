package com.gcplot.repository;

import com.gcplot.commons.Range;
import com.gcplot.model.gc.GCEvent;

import java.util.Iterator;
import java.util.List;

public interface GCEventRepository {

    List<GCEvent> events(String analyseId, String jvmId, Range range);

    Iterator<GCEvent> lazyEvents(String analyseId, String jvmId, Range range);

    void erase(String analyseId, String jvmId, Range range);

    void erase(String analyseId, List<String> jvmId, Range range);

    void add(GCEvent event);

    void add(List<GCEvent> events);

}
