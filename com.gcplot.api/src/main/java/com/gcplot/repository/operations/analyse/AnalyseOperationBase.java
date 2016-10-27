package com.gcplot.repository.operations.analyse;

import com.gcplot.Identifier;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         10/26/16
 */
public abstract class AnalyseOperationBase implements AnalyseOperation {
    private final Identifier accountId;
    private final String analyseId;

    @Override
    public String analyseId() {
        return analyseId;
    }

    @Override
    public Identifier accountId() {
        return accountId;
    }

    public AnalyseOperationBase(Identifier accountId, String analyseId) {
        this.accountId = accountId;
        this.analyseId = analyseId;
    }
}
