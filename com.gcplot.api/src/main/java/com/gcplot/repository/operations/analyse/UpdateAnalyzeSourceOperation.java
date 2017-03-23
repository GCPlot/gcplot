package com.gcplot.repository.operations.analyse;

import com.gcplot.Identifier;
import com.gcplot.model.gc.SourceType;
import com.gcplot.repository.operations.OperationType;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/23/17
 */
public class UpdateAnalyzeSourceOperation extends AnalyseOperationBase {
    private final SourceType sourceType;
    private final String sourceConfig;

    public SourceType getSourceType() {
        return sourceType;
    }

    public String getSourceConfig() {
        return sourceConfig;
    }

    public UpdateAnalyzeSourceOperation(Identifier accountId, String analyzeId,
                                        SourceType sourceType, String sourceConfig) {
        super(accountId, analyzeId);
        this.sourceType = sourceType;
        this.sourceConfig = sourceConfig;
    }

    @Override
    public OperationType type() {
        return OperationType.UPDATE_ANALYZE_SOURCE;
    }
}
