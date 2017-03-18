package com.gcplot.services.logs;

import com.gcplot.commons.exceptions.Exceptions;
import com.gcplot.logs.LogHandle;
import com.gcplot.logs.LogSource;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         2/26/17
 */
public abstract class BaseLogSource implements LogSource {
    protected LogHandle handle;

    @Override
    public LogHandle handle() {
        return handle;
    }

    @Override
    public InputStream logStream() {
        try {
            InputStream fis = inputStream();
            if (isGzipped()) {
                fis = new GZIPInputStream(fis);
            } else {
                fis = new BufferedInputStream(fis);
            }
            return fis;
        } catch (Throwable t) {
            throw Exceptions.runtime(t);
        }
    }

}
