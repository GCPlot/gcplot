package com.gcplot.repository;

import com.gcplot.commons.Range;
import com.gcplot.model.JVMEvent;

import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/3/16
 */
public interface JVMEventsRepository<T extends JVMEvent> {

    List<T> events(String analyseId, String jvmId, Range range);

    Iterator<T> lazyEvents(String analyseId, String jvmId, Range range);

    void erase(String analyseId, String jvmId, Range range);

    void erase(String analyseId, List<String> jvmIds, Range range);

    void add(T event);

    void add(List<T> events);

    void addAsync(T event);

    void addAsync(List<T> events);

}
