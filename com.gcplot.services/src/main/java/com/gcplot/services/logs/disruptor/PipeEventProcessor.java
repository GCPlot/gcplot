package com.gcplot.services.logs.disruptor;

import com.gcplot.logs.ParserContext;
import com.gcplot.logs.mapping.Mapper;
import com.gcplot.model.gc.GCEvent;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LiteBlockingWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         6/14/17
 */
public class PipeEventProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PipeEventProcessor.class);
    private final Consumer<List<GCEvent>> persister;
    private final Consumer<GCEvent> singlePersister;
    private final Mapper eventMapper;
    private Disruptor<GCEventBundle> disruptor;

    public PipeEventProcessor(Consumer<List<GCEvent>> persister, Consumer<GCEvent> singlePresister,
                              Mapper eventMapper) {
        this.persister = persister;
        this.singlePersister = singlePresister;
        this.eventMapper = eventMapper;
    }

    public void init() {
        disruptor = new Disruptor<>(GCEventBundle::new, 32 * 1024, new ThreadFactoryBuilder()
                .setDaemon(false).setNameFormat("ds-").build(), ProducerType.SINGLE, new BlockingWaitStrategy());
        EventHandlerGroup group = null;
        if (eventMapper != Mapper.EMPTY) {
            int mapperCount = Math.max(Runtime.getRuntime().availableProcessors() / 3, 1);
            EventHandler<GCEventBundle>[] mapperHandlers = new EventHandler[mapperCount];
            for (int i = 0; i < mapperCount; i++) {
                mapperHandlers[i] = (e, sequence, endOfBatch) -> {
                    try {
                        e.event = eventMapper.map(e.parserContext, e.rawEvent);
                        if (e.event == null) {
                            e.ignore();
                        }
                    } catch (Throwable t) {
                        e.ignore();
                        LOG.error(t.getMessage(), t);
                    }
                };
            }
            group = disruptor.handleEventsWith(mapperHandlers);
        }
        EventHandler<GCEventBundle> eventHandler = (e, sequence, endOfBatch) -> {
            if (!e.isIgnore) {
                e.event.analyseId(e.parserContext.analysisId()).jvmId(e.parserContext.jvmId());
                if (e.parsingState.getFirstEvent() == null && !e.event.isOther()) {
                    e.parsingState.setFirstEvent(e.event);
                }
                if (e.event.isOther() || (e.parsingState.getLastPersistedEvent().get() != null &&
                        e.parsingState.getLastPersistedEvent().get().timestamp() >= e.event.timestamp())) {
                    e.ignore();
                } else {
                    e.parsingState.setLastEvent(e.event);
                }
            }
        };
        if (group != null) {
            group = group.then(eventHandler);
        } else {
            group = disruptor.handleEventsWith(eventHandler);
        }
        int persisterCount = Runtime.getRuntime().availableProcessors() * 2;
        EventHandler<GCEventBundle>[] persisterHandlers = new EventHandler[persisterCount];
        for (int i = 0; i < persisterCount; i++) {
            persisterHandlers[i] = new EventHandler<GCEventBundle>() {
                private static final int MAX_BATCH_SIZE = 5;
                private List<GCEvent> batch = new ArrayList<>(MAX_BATCH_SIZE);
                private int monthsSum;

                @Override
                public void onEvent(GCEventBundle e, long sequence, boolean endOfBatch) throws Exception {
                    try {
                        if (batch.size() > 0 && (batch.size() == MAX_BATCH_SIZE || endOfBatch)) {
                            if (monthsSum % batch.get(0).occurred().getMonthOfYear() != 0) {
                                batch.forEach(singlePersister);
                            } else {
                                persister.accept(new ArrayList<>(batch));
                            }
                            batch.clear();
                            monthsSum = 0;
                        }
                        if (!e.isIgnore) {
                            if (endOfBatch) {
                                singlePersister.accept(e.event);
                            } else {
                                batch.add(e.event);
                                monthsSum += e.event.occurred().getMonthOfYear();
                            }
                        }
                    } catch (Throwable t) {
                        LOG.error(t.getMessage(), t);
                    } finally {
                        e.future.complete(e.rawEvent);
                    }
                }
            };
        }
        group.then(persisterHandlers);
        LOG.info("Starting Pipe Event Processor ...");
        disruptor.start();
        LOG.info("Pipe Event Processor started!");
    }

    public void shutdown() {
        LOG.info("Shutting down Pipe Event Processor ...");
        disruptor.shutdown();
        LOG.info("Pipe Event Processor stopped.");
    }

    public Future processNext(Object rawEvent, ParserContext context, ParsingState commonState) {
        final CompletableFuture future = new CompletableFuture();
        disruptor.publishEvent((event, sequence) -> {
            event.reset().future(future).parsingState(commonState).rawEvent(rawEvent).parserContext(context);
        });
        return future;
    }

}
