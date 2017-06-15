package com.gcplot.services.logs.disruptor;

import com.gcplot.commons.LazyVal;
import com.gcplot.logs.ParserContext;
import com.gcplot.model.gc.GCEvent;
import com.gcplot.repository.GCEventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         6/14/17
 */
public class ParsingState {
    public static final int MAX_BATCH_SIZE = 5;
    private final LazyVal<GCEvent> lastPersistedEvent;
    private GCEvent firstEvent;
    private GCEvent lastEvent;
    private ThreadLocal<List<GCEvent>> batch = ThreadLocal.withInitial(() -> new ArrayList<GCEvent>(MAX_BATCH_SIZE));
    private ThreadLocal<List<CompletableFuture>> futures = ThreadLocal.withInitial(() -> new ArrayList<CompletableFuture>(MAX_BATCH_SIZE));
    private ThreadLocal<Integer> monthsSum = ThreadLocal.withInitial(() -> 0);

    public ParsingState(ParserContext ctx, GCEventRepository repository, String checksum) {
        this.lastPersistedEvent = LazyVal.ofOpt(() ->
                repository.lastEvent(ctx.analysisId(), ctx.jvmId(), checksum, getFirstEvent().occurred().minusDays(1)));
    }

    public LazyVal<GCEvent> getLastPersistedEvent() {
        return lastPersistedEvent;
    }

    public GCEvent getFirstEvent() {
        return firstEvent;
    }

    public void setFirstEvent(GCEvent firstEvent) {
        this.firstEvent = firstEvent;
    }

    public GCEvent getLastEvent() {
        return lastEvent;
    }

    public void setLastEvent(GCEvent lastEvent) {
        this.lastEvent = lastEvent;
    }

    public ThreadLocal<List<GCEvent>> getBatch() {
        return batch;
    }

    public ThreadLocal<List<CompletableFuture>> getFutures() {
        return futures;
    }

    public ThreadLocal<Integer> getMonthsSum() {
        return monthsSum;
    }
}
