package com.gcplot.model;

import com.gcplot.model.gc.ObjectsAges;
import com.gcplot.model.gc.ObjectsAgesFactory;
import com.gcplot.model.gc.ObjectsAgesImpl;
import org.joda.time.DateTime;

import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         2/27/17
 */
public class DefaultObjectsAgesFactory implements ObjectsAgesFactory {

    @Override
    public ObjectsAges create(String analyzeId, String jvmId, DateTime occurred,
                              long desiredSurvivorSize, List<Long> occupied, List<Long> total, String ext) {
        return new ObjectsAgesImpl()
                .analyseId(analyzeId)
                .jvmId(jvmId)
                .occurred(occurred)
                .desiredSurvivorSize(desiredSurvivorSize)
                .occupied(occupied)
                .total(total)
                .ext(ext);
    }

}
