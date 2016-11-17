package com.gcplot.model.gc;

import com.gcplot.model.DatedEvent;
import com.gcplot.model.VMEvent;

import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/31/16
 */
public interface ObjectsAges extends VMEvent, DatedEvent {

    long desiredSurvivorSize();

    List<Long> occupied();

    List<Long> total();

    String ext();

}
