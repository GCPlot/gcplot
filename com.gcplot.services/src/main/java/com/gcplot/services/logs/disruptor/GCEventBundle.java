package com.gcplot.services.logs.disruptor;

import com.gcplot.logs.ParserContext;
import com.gcplot.model.gc.GCEvent;

import java.util.concurrent.CompletableFuture;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         6/14/17
 */
public class GCEventBundle {

    public Object rawEvent;
    public GCEvent event;
    public boolean isIgnore;
    public CompletableFuture future;
    public ParsingState parsingState;
    public ParserContext parserContext;
    public boolean isControl;

    public GCEventBundle reset() {
        rawEvent = null;
        event = null;
        parsingState = null;
        future = null;
        parserContext = null;
        isIgnore = false;
        isControl = false;
        return this;
    }

    public GCEventBundle rawEvent(Object rawEvent) {
        this.rawEvent = rawEvent;
        return this;
    }

    public GCEventBundle event(GCEvent event) {
        this.event = event;
        return this;
    }

    public GCEventBundle ignore() {
        this.isIgnore = true;
        return this;
    }

    public GCEventBundle future(CompletableFuture future) {
        this.future = future;
        return this;
    }

    public GCEventBundle parsingState(ParsingState parsingState) {
        this.parsingState = parsingState;
        return this;
    }

    public GCEventBundle parserContext(ParserContext parserContext) {
        this.parserContext = parserContext;
        return this;
    }

    public GCEventBundle control() {
        this.isControl = true;
        return this;
    }
}
