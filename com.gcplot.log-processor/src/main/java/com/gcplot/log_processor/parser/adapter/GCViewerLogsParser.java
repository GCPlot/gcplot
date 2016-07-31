package com.gcplot.log_processor.parser.adapter;

import com.gcplot.log_processor.parser.LogsParser;
import com.gcplot.log_processor.parser.ParseResult;
import com.gcplot.model.gc.GCAnalyse;
import com.gcplot.model.gc.GCEvent;
import com.gcplot.model.gc.GarbageCollectorType;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/24/16
 */
public class GCViewerLogsParser implements LogsParser {

    @Override
    public ParseResult parse(InputStream reader, GarbageCollectorType type, Consumer<GCEvent> eventsConsumer) {
        return null;
    }

}
