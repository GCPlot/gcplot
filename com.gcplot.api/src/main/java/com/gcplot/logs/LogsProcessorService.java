package com.gcplot.logs;

import com.gcplot.model.account.Account;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         2/25/17
 */
public interface LogsProcessorService {

    LogProcessResult process(LogSource log, Account account, String analyzeId, String jvmId,
                             boolean sync);

}
