package com.gcplot.logs;

import com.gcplot.model.account.Account;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         2/25/17
 */
public interface LogsProcessorService {
    String ANONYMOUS_ANALYSE_ID = "7acada7b-e109-4d11-ac01-b3521d9d58c3";

    LogProcessResult process(LogSource log, Account account, String analyzeId, String jvmId,
                             boolean sync);

}
