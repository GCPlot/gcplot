package com.gcplot.repository.operations.analyse;

import com.gcplot.Identifier;
import com.gcplot.model.gc.MemoryDetails;
import com.gcplot.repository.operations.OperationType;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         10/26/16
 */
public class UpdateJvmInfoOperation extends AnalyseOperationBase {
    private final String jvmId;
    private final String headers;
    private final MemoryDetails memoryDetails;

    public String getJvmId() {
        return jvmId;
    }

    public String getHeaders() {
        return headers;
    }

    public MemoryDetails getMemoryDetails() {
        return memoryDetails;
    }

    public UpdateJvmInfoOperation(Identifier accountId, String analyseId, String jvmId, String headers,
                                  MemoryDetails memoryDetails) {
        super(accountId, analyseId);
        this.jvmId = jvmId;
        this.headers = headers;
        this.memoryDetails = memoryDetails;
    }

    @Override
    public OperationType type() {
        return OperationType.UPDATE_JVM_INFO;
    }
}
