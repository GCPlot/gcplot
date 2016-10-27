package com.gcplot.repository.operations.analyse;

import com.gcplot.Identifier;
import com.gcplot.repository.operations.OperationType;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         10/26/16
 */
public class UpdateAnalyseOperation extends AnalyseOperationBase {
    private final String name;
    private final String ext;

    public String getName() {
        return name;
    }

    public String getExt() {
        return ext;
    }

    public UpdateAnalyseOperation(Identifier accountId, String analyseId, String name, String ext) {
        super(accountId, analyseId);
        this.name = name;
        this.ext = ext;
    }

    @Override
    public OperationType type() {
        return OperationType.UPDATE_ANALYSE;
    }
}
