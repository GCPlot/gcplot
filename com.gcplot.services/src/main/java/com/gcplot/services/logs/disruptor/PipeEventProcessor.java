package com.gcplot.services.logs.disruptor;

import com.gcplot.logs.ParserContext;
import com.gcplot.logs.mapping.Mapper;
import com.gcplot.model.gc.GCEvent;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         6/14/17
 */
public class PipeEventProcessor {
    private final Consumer<GCEvent> persister;
    private final Mapper eventMapper;
    private Disruptor<GCEventBundle> disruptor;

    public PipeEventProcessor(Consumer<GCEvent> persister, Mapper eventMapper) {
        this.persister = persister;
        this.eventMapper = eventMapper;
    }

    public void init() {
        disruptor = new Disruptor<>(GCEventBundle::new, 8 * 1024, new ThreadFactoryBuilder()
                .setDaemon(false).setNameFormat("ds-").build(), ProducerType.SINGLE, new YieldingWaitStrategy());
        if (eventMapper != Mapper.EMPTY) {
            int mapperCount = Math.max(Runtime.getRuntime().availableProcessors() - 1, 1);
            EventHandler<GCEventBundle>[] mapperHandlers = new EventHandler[mapperCount];
            for (int i = 0; i < mapperCount; i++) {
                mapperHandlers[i] = (event, sequence, endOfBatch) -> {
                    event.event = eventMapper.map()
                };
            }
        }
    }

    public void shutdown() {
        disruptor.shutdown();
    }

    public Future processNext(Object rawEvent, ParserContext context, ParsingState commonState) {
        final CompletableFuture future = new CompletableFuture();

        return future;
    }

}
