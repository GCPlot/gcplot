package com.gcplot.fs;

import com.gcplot.logs.LogHandle;
import com.gcplot.logs.LogSource;

import java.util.Iterator;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/6/17
 */
public interface LogsStorage {

    Iterator<LogHandle> listAll();

    LogSource get(LogHandle handle);

    void delete(LogHandle handle);

}
