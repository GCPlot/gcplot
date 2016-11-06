package com.gcplot.repository.operations.analyse;

import com.gcplot.Identifier;
import com.gcplot.repository.operations.OperationType;
import org.joda.time.DateTime;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         10/26/16
 */
public class UpdateLastEventOperation extends AnalyseOperationBase {
    private final String jvmId;
    private final DateTime lastEvent;

    public String getJvmId() {
        return jvmId;
    }

    public DateTime getLastEvent() {
        return lastEvent;
    }

    public UpdateLastEventOperation(Identifier accountId, String analyseId, String jvmId, DateTime lastEvent) {
        super(accountId, analyseId);
        this.jvmId = jvmId;
        this.lastEvent = lastEvent;
    }

    @Override
    public OperationType type() {
        return OperationType.UPDATE_LAST_EVENT;
    }
}
