package com.gcplot.log_processor.parser;

import com.gcplot.model.gc.GCAnalyse;
import com.gcplot.model.gc.GCEvent;

import java.io.InputStreamReader;
import java.util.function.Consumer;

public interface LogsParser {

    /**
     * Parses the input GC log stream and produces appropriate GC Events.
     *
     * @param reader raw GC log data
     * @param analyse an analyse, of which part this log stream is
     * @param eventsConsumer the consumer of newly produced events
     * @return the number of events produced
     */
    long parse(InputStreamReader reader, GCAnalyse analyse, Consumer<GCEvent> eventsConsumer);

}
