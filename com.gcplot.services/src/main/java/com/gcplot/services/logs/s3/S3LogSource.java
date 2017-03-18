package com.gcplot.services.logs.s3;

import com.amazonaws.services.s3.model.S3Object;
import com.gcplot.logs.LogHandle;
import com.gcplot.logs.LogSource;
import com.gcplot.services.logs.BaseLogSource;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/1/17
 */
public class S3LogSource extends BaseLogSource implements LogSource {
    private S3Object object;

    public S3LogSource(LogHandle handle, S3Object object) {
        this.handle = handle;
        this.object = object;
    }

    @Override
    public InputStream inputStream() {
        return object.getObjectContent();
    }

    @Override
    public boolean isGzipped() {
        return handle.getName().endsWith(".gz");
    }

    @Override
    public Optional<File> localFile() {
        return Optional.empty();
    }

    @Override
    public String checksum() {
        return handle.getName().substring(0, handle.getName().lastIndexOf('.'));
    }
}
