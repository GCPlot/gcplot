package com.gcplot.logs;

import com.gcplot.logs.mapping.Mapper;
import com.gcplot.model.gc.GCEvent;

import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface LogsParser<RawEventType> {

    /**
     * Parses the input GC log stream and produces appropriate GC Events.
     */
    ParseResult parse(InputStream reader, Consumer<RawEventType> eventsConsumer, ParserContext parserContext);

    Mapper<RawEventType> getMapper();

}
