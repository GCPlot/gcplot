package com.gcplot.logs;

import com.gcplot.model.gc.GCEvent;

import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface LogsParser {

    /**
     * Parses the input GC log stream and produces appropriate GC Events.
     */
    ParseResult parse(InputStream reader, Predicate<GCEvent> firstEvent,
                      Consumer<GCEvent> eventsConsumer, ParserContext parserContext);

}
