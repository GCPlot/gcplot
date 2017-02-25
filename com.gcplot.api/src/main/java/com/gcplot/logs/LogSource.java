package com.gcplot.logs;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         2/25/17
 */
public interface LogSource {

    InputStream inputStream();

    InputStream logStream();

    String name();

    boolean isGzipped();

    Optional<File> localFile();

    Optional<String> checksum();

}
