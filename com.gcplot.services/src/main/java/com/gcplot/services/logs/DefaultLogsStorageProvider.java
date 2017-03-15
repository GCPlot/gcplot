package com.gcplot.services.logs;

import com.gcplot.fs.LogsStorage;
import com.gcplot.fs.LogsStorageProvider;
import com.gcplot.model.gc.SourceType;

import java.util.Properties;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/14/17
 */
public class DefaultLogsStorageProvider implements LogsStorageProvider {

    @Override
    public LogsStorage internal() {
        return null;
    }

    @Override
    public LogsStorage get(SourceType type, Properties config) {
        return null;
    }
}
