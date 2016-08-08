package com.gcplot.log_processor.parser.adapter;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/8/16
 */
public interface StreamDataReader {

    StreamGCModel readStream() throws IOException;

    void excludedHandler(Consumer<String> c);

    void headerHandler(Consumer<String> c);

}
