package com.gcplot.log_processor.parser.detect;

import com.gcplot.model.VMVersion;
import com.gcplot.model.gc.GarbageCollectorType;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         10/30/16
 */
public class VMProperties {
    public static final VMProperties EMPTY = new VMProperties(null, null);
    private final VMVersion version;
    private final GarbageCollectorType gcType;

    public VMVersion getVersion() {
        return version;
    }

    public GarbageCollectorType getGcType() {
        return gcType;
    }

    public VMProperties(VMVersion version, GarbageCollectorType gcType) {
        this.version = version;
        this.gcType = gcType;
    }
}
