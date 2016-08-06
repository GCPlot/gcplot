package com.gcplot.log_processor.parser;

import com.gcplot.model.gc.GCEvent;
import com.gcplot.model.gc.GarbageCollectorType;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.function.Consumer;

public interface LogsParser {

    /**
     * Parses the input GC log stream and produces appropriate GC Events.
     *
     * @param reader raw GC log data
     * @param log special consumer of the whole parsing trace data
     * @param type
     * @param eventsConsumer the consumer of newly produced events
     * @return the number of events produced
     */
    ParseResult parse(InputStream reader, Logger log,
                      GarbageCollectorType type,
                      Consumer<GCEvent> eventsConsumer);

}
