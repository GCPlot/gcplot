package com.gcplot.services.logs.sources;

import com.gcplot.commons.FileUtils;
import com.gcplot.commons.exceptions.Exceptions;
import com.gcplot.logs.LogHandle;
import com.gcplot.logs.LogSource;
import com.gcplot.services.logs.BaseLogSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         2/25/17
 */
public class FileLogSource extends BaseLogSource implements LogSource {
    private static final Logger LOG = LoggerFactory.getLogger(FileLogSource.class);
    private File file;
    private String checksum;
    private LogHandle handle;

    public FileLogSource(File file, LogHandle handle) {
        this.file = file;
        this.handle = handle;
    }

    @Override
    public InputStream inputStream() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw Exceptions.runtime(e);
        }
    }

    @Override
    public LogHandle handle() {
        return handle;
    }

    @Override
    public boolean isGzipped() {
        try (InputStream in = inputStream()){
            final int b1 = in.read();
            final int b2 = in.read();
            if (b2 < 0) {
                throw new EOFException();
            }
            int firstBytes = (b2 << 8) | b1;
            return firstBytes == GZIPInputStream.GZIP_MAGIC;
        } catch (Throwable t) {
            throw Exceptions.runtime(t);
        }
    }

    @Override
    public Optional<File> localFile() {
        return Optional.of(file);
    }

    @Override
    public String checksum() {
        if (checksum == null) {
            try {
                LOG.warn("No checksum was provided in the request, calculating own: {}", localFile());
                try (InputStream is = logStream()) {
                    checksum = FileUtils.getFileChecksum(MessageDigest.getInstance("MD5"), is);
                }
                LOG.warn("The calculated checksum for {} is {}", handle.getName(), checksum);
            } catch (Throwable t) {
                throw Exceptions.runtime(t);
            }
        }
        return checksum;
    }
}
