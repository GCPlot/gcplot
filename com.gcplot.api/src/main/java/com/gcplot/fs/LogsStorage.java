package com.gcplot.fs;

import com.gcplot.Identifier;
import com.gcplot.logs.LogHandle;
import com.gcplot.logs.LogSource;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/6/17
 */
public interface LogsStorage {

    Iterable<LogHandle> listAll();

    LogSource get(LogHandle handle);

    void delete(LogHandle handle);

}
