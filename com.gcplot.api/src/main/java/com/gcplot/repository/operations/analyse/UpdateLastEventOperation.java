package com.gcplot.repository.operations.analyse;

import com.gcplot.Identifier;
import com.gcplot.repository.operations.OperationType;
import org.joda.time.DateTime;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         10/26/16
 */
public class UpdateLastEventOperation extends AnalyseOperationBase {
    private final DateTime lastEvent;

    public DateTime getLastEvent() {
        return lastEvent;
    }

    public UpdateLastEventOperation(Identifier accountId, String analyseId, DateTime lastEvent) {
        super(accountId, analyseId);
        this.lastEvent = lastEvent;
    }

    @Override
    public OperationType type() {
        return OperationType.UPDATE_LAST_EVENT;
    }
}
