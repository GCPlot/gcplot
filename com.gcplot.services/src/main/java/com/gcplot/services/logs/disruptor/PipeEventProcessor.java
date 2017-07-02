package com.gcplot.services.logs.disruptor;

import com.gcplot.logs.ParserContext;
import com.gcplot.logs.mapping.Mapper;
import com.gcplot.model.gc.GCEvent;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         6/14/17
 */
public class PipeEventProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PipeEventProcessor.class);
    private static final Object DUMMY = new Object();
    private final Consumer<List<GCEvent>> persister;
    private final Consumer<GCEvent> singlePersister;
    private final Mapper eventMapper;
    private Disruptor<GCEventBundle> input;
    private Disruptor<GCEventBundle> output;

    public PipeEventProcessor(Consumer<List<GCEvent>> persister, Consumer<GCEvent> singlePersister,
                              Mapper eventMapper) {
        this.persister = persister;
        this.singlePersister = singlePersister;
        this.eventMapper = eventMapper;
    }

    public void init() {
        input = new Disruptor<>(GCEventBundle::new, 8 * 1024, new ThreadFactoryBuilder()
                .setDaemon(false).setNameFormat("ds-in-%d").build(), ProducerType.MULTI, new BlockingWaitStrategy());
        output = new Disruptor<>(GCEventBundle::new, 16 * 1024, new ThreadFactoryBuilder()
                .setDaemon(false).setNameFormat("ds-out-%d").build(), ProducerType.MULTI, new BlockingWaitStrategy());
        EventHandlerGroup group = null;
        if (eventMapper != Mapper.EMPTY) {
            final long mapperCount = Math.max(Runtime.getRuntime().availableProcessors() / 2, 1);
            EventHandler<GCEventBundle>[] mapperHandlers = new EventHandler[(int) mapperCount];
            for (int i = 0; i < mapperCount; i++) {
                final long s = i;
                mapperHandlers[i] = (e, sequence, endOfBatch) -> {
                    if (!e.isControl && sequence % mapperCount == s) {
                        try {
                            e.event = eventMapper.map(e.parserContext, e.rawEvent);
                            if (e.event == null) {
                                e.ignore();
                            }
                        } catch (Throwable t) {
                            e.ignore();
                            LOG.error(t.getMessage(), t);
                        }
                    }
                };
            }
            group = input.handleEventsWith(mapperHandlers);
        }
        EventHandler<GCEventBundle> eventHandler = (e, sequence, endOfBatch) -> {
            try {
                final ParsingState parsingState = e.parsingState;
                if (e.isControl) {
                    if (e.parsingState.getYoungSampler() != null) {
                        e.parsingState.getYoungSampler().complete().forEach(event -> persistEvent(event, parsingState));
                    }
                    e.future.complete(DUMMY);
                } else if (!e.isIgnore) {
                    e.event.analyseId(e.parserContext.analysisId()).jvmId(e.parserContext.jvmId());
                    if (e.parsingState.getFirstEvent() == null && !e.event.isOther()) {
                        e.parsingState.setFirstEvent(e.event);
                    }
                    if (!e.event.isOther() && (e.parsingState.getLastPersistedEvent().get() == null ||
                            e.parsingState.getLastPersistedEvent().get().occurred().getMillis() < e.event.occurred().getMillis())) {
                        e.parsingState.setLastEvent(e.event);

                        if (e.parsingState.getYoungSampler() == null ||
                                !e.parsingState.getYoungSampler().isApplicable(e.event)) {
                            persistEvent(e.event, parsingState);
                        } else {
                            List<GCEvent> gcs = e.parsingState.getYoungSampler().process(e.event);
                            if (gcs.size() > 0) {
                                gcs.forEach(event -> persistEvent(event, parsingState));
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                LOG.error(t.getMessage(), t);
            }
        };
        if (group != null) {
            group.then(eventHandler);
        } else {
            input.handleEventsWith(eventHandler);
        }
        long persisterCount = Runtime.getRuntime().availableProcessors() * 2;
        EventHandler<GCEventBundle>[] persisterHandlers = new EventHandler[(int) persisterCount];
        for (int i = 0; i < persisterCount; i++) {
            final long s = i;
            persisterHandlers[i] = new EventHandler<GCEventBundle>() {
                @Override
                public void onEvent(GCEventBundle e, long sequence, boolean endOfBatch) throws Exception {
                    try {
                        ParsingState ps = e.parsingState;
                        List<GCEvent> batch = ps.getBatch().get();
                        if (!e.isControl && sequence % persisterCount == s) {
                            if (batch.size() > 0 && (batch.size() == ParsingState.MAX_BATCH_SIZE || endOfBatch)) {
                                persist(ps, batch);
                            }
                            if (endOfBatch) {
                                singlePersister.accept(e.event);
                            } else {
                                batch.add(e.event);
                                ps.getMonthsSum().set(ps.getMonthsSum().get() + e.event.occurred().getMonthOfYear());
                            }
                        } else if ((endOfBatch || e.isControl) && batch.size() > 0) {
                            persist(ps, batch);
                        }
                    } catch (Throwable t) {
                        LOG.error(t.getMessage(), t);
                    } finally {
                        if (e.isControl) {
                            e.future.complete(DUMMY);
                        }
                    }
                }

                private void persist(ParsingState ps, List<GCEvent> batch) {
                    if (ps.getMonthsSum().get() %
                            batch.get(0).occurred().getMonthOfYear() != 0) {
                        batch.forEach(singlePersister);
                    } else {
                        persister.accept(new ArrayList<>(batch));
                    }
                    batch.clear();
                    ps.getMonthsSum().set(0);
                }
            };
        }
        output.handleEventsWith(persisterHandlers);

        LOG.info("Starting Pipe Event Processor ...");
        input.start();
        output.start();
        LOG.info("Pipe Event Processor started!");
    }

    public void shutdown() {
        LOG.info("Shutting down Pipe Event Processor ...");
        input.shutdown();
        output.shutdown();
        LOG.info("Pipe Event Processor stopped.");
    }

    public void processNext(Object rawEvent, ParserContext context, ParsingState commonState) {
        input.publishEvent((event, sequence) ->
                event.reset().parsingState(commonState).rawEvent(rawEvent).parserContext(context));
    }

    public void finish(ParsingState commonState) {
        final CompletableFuture futureIn = new CompletableFuture();
        final CompletableFuture futureOut = new CompletableFuture();
        input.publishEvent((event, sequence) -> event.reset().future(futureIn).control().parsingState(commonState));
        try {
            futureIn.get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error(e.getMessage(), e);
        }
        output.publishEvent((event, sequence) -> event.reset().future(futureOut).control().parsingState(commonState));
        try {
            futureOut.get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void persistEvent(GCEvent gcEvent, ParsingState parsingState) {
        output.publishEvent((event, s) -> event.reset().event(gcEvent).parsingState(parsingState));
    }
}
