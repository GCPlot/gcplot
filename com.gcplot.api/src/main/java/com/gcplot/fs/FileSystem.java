package com.gcplot.fs;

import com.gcplot.Identifier;
import com.gcplot.logs.LogSource;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/6/17
 */
public interface FileSystem {

    Iterable<LogSource> listLogs(Identifier account, String analyzeId, String jvmId);

}
