package com.gcplot.repository;

import com.gcplot.commons.Range;
import com.gcplot.model.VMEvent;
import org.joda.time.DateTime;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/3/16
 */
public interface VMEventsRepository<T extends VMEvent> {

    Optional<T> lastEvent(String analyseId, String jvmId, DateTime start);

    List<T> events(String analyseId, String jvmId, Range range);

    Iterator<T> lazyEvents(String analyseId, String jvmId, Range range);

    void erase(String analyseId, String jvmId, Range range);

    void erase(String analyseId, List<String> jvmIds, Range range);

    void add(T event);

    void add(List<T> events);

    void addAsync(T event);

    void addAsync(List<T> events);

}
