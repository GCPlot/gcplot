package com.gcplot.log_processor.parser;

import com.gcplot.model.gc.GCEvent;
import com.gcplot.model.gc.GarbageCollectorType;

import java.io.InputStream;
import java.util.function.Consumer;

public interface LogsParser {

    /**
     * Parses the input GC log stream and produces appropriate GC Events.
     *
     * @param reader raw GC log data
     * @param eventsConsumer the consumer of newly produced events
     * @return the number of events produced
     */
    ParseResult parse(InputStream reader, GarbageCollectorType type, Consumer<GCEvent> eventsConsumer);

}
