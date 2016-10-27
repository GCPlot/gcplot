package com.gcplot.repository.operations.analyse;

import com.gcplot.Identifier;
import com.gcplot.model.VMVersion;
import com.gcplot.model.gc.GarbageCollectorType;
import com.gcplot.repository.operations.OperationType;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         10/26/16
 */
public class UpdateJvmVersionOperation extends AnalyseOperationBase {
    private final String jvmId;
    private final VMVersion version;
    private final GarbageCollectorType type;

    public String getJvmId() {
        return jvmId;
    }

    public VMVersion getVersion() {
        return version;
    }

    public GarbageCollectorType getType() {
        return type;
    }

    public UpdateJvmVersionOperation(Identifier accountId, String analyseId, String jvmId,
                                     VMVersion version, GarbageCollectorType type) {
        super(accountId, analyseId);
        this.jvmId = jvmId;
        this.version = version;
        this.type = type;
    }

    @Override
    public OperationType type() {
        return OperationType.UPDATE_JVM_VERSION;
    }
}
