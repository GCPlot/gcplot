package com.gcplot.model.gc;

import org.joda.time.DateTime;

import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         2/27/17
 */
public interface ObjectsAgesFactory {

    ObjectsAges create(String analyzeId, String jvmId, DateTime occurred,
                       long desiredSurvivorSize, List<Long> occupied, List<Long> total, String ext);

}
