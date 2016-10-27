package com.gcplot.repository.operations.analyse;

import com.gcplot.Identifier;
import com.gcplot.repository.operations.OperationType;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         10/26/16
 */
public class RemoveJvmOperation extends AnalyseOperationBase {
    private final String jvmId;

    public String getJvmId() {
        return jvmId;
    }

    public RemoveJvmOperation(Identifier accountId, String analyseId, String jvmId) {
        super(accountId, analyseId);
        this.jvmId = jvmId;
    }

    @Override
    public OperationType type() {
        return OperationType.REMOVE_JVM;
    }
}
