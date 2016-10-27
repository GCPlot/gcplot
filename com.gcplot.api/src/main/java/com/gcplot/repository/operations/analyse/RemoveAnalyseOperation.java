package com.gcplot.repository.operations.analyse;

import com.gcplot.Identifier;
import com.gcplot.repository.operations.OperationType;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         10/26/16
 */
public class RemoveAnalyseOperation extends AnalyseOperationBase {

    public RemoveAnalyseOperation(Identifier accountId, String analyseId) {
        super(accountId, analyseId);
    }

    @Override
    public OperationType type() {
        return OperationType.REMOVE_ANALYSE;
    }
}
