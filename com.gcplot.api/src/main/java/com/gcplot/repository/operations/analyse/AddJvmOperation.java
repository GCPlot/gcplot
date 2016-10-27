package com.gcplot.repository.operations.analyse;

import com.gcplot.Identifier;
import com.gcplot.model.VMVersion;
import com.gcplot.model.gc.GarbageCollectorType;
import com.gcplot.model.gc.MemoryDetails;
import com.gcplot.repository.operations.OperationType;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         10/26/16
 */
public class AddJvmOperation extends AnalyseOperationBase {
    private final String jvmId;
    private final VMVersion version;
    private final GarbageCollectorType type;
    private final String headers;
    private final MemoryDetails memoryDetails;

    public String getJvmId() {
        return jvmId;
    }

    public VMVersion getVersion() {
        return version;
    }

    public GarbageCollectorType getType() {
        return type;
    }

    public String getHeaders() {
        return headers;
    }

    public MemoryDetails getMemoryDetails() {
        return memoryDetails;
    }

    public AddJvmOperation(Identifier accountId, String analyseId, String jvmId,
                           VMVersion version, GarbageCollectorType type,
                           String headers, MemoryDetails memoryDetails) {
        super(accountId, analyseId);
        this.jvmId = jvmId;
        this.version = version;
        this.type = type;
        this.headers = headers;
        this.memoryDetails = memoryDetails;
    }

    @Override
    public OperationType type() {
        return OperationType.ADD_JVM;
    }
}
