package com.gcplot.log_processor.parser;

import com.gcplot.model.gc.GarbageCollectorType;
import org.joda.time.DateTime;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * Had to move some parameters, which must be passed to the parser, into
 * the special Context, according to well known pattern for such cases -
 * the number of parameters is too high.
 *
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/6/16
 */

public class ParserContext {

    private final Logger logger;
    public Logger logger() {
        return logger;
    }

    private final GarbageCollectorType collectorType;
    public GarbageCollectorType collectorType() {
        return collectorType;
    }

    public ParserContext(Logger logger, GarbageCollectorType collectorType) {
        this.logger = logger;
        this.collectorType = collectorType;
    }

}
