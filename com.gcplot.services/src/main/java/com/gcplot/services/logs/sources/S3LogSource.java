package com.gcplot.services.logs.sources;

import com.gcplot.logs.LogSource;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/1/17
 */
public class S3LogSource implements LogSource {



    @Override
    public InputStream inputStream() {
        return null;
    }

    @Override
    public InputStream logStream() {
        return null;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public boolean isGzipped() {
        return false;
    }

    @Override
    public Optional<File> localFile() {
        return null;
    }

    @Override
    public String checksum() {
        return null;
    }
}
