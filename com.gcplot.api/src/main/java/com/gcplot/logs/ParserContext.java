package com.gcplot.logs;

import com.gcplot.model.VMVersion;
import com.gcplot.model.gc.GarbageCollectorType;
import org.slf4j.Logger;

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

    private final String streamChecksum;
    public String streamChecksum() {
        return streamChecksum;
    }

    private final GarbageCollectorType collectorType;
    public GarbageCollectorType collectorType() {
        return collectorType;
    }

    private final VMVersion vmVersion;
    public VMVersion vmVersion() {
        return vmVersion;
    }

    public ParserContext(Logger logger, String streamChecksum,
                         GarbageCollectorType collectorType, VMVersion vmVersion) {
        this.logger = logger;
        this.streamChecksum = streamChecksum;
        this.collectorType = collectorType;
        this.vmVersion = vmVersion;
    }

}
