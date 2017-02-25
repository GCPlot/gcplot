package com.gcplot.logs;

import com.gcplot.model.gc.GCEvent;

import java.io.InputStream;
import java.util.function.Consumer;

public interface LogsParser {

    /**
     * Parses the input GC log stream and produces appropriate GC Events.
     */
    ParseResult parse(InputStream reader, Consumer<GCEvent> firstEvent, Consumer<GCEvent> lastEvent,
                      Consumer<GCEvent> eventsConsumer, ParserContext parserContext);

}
