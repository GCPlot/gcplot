package com.gcplot.log_processor.parser.adapter;

import com.gcplot.configuration.ConfigurationManager;
import com.gcplot.log_processor.parser.ParseResult;
import com.gcplot.log_processor.parser.producers.v8.MetadataInfoProducer;
import com.gcplot.log_processor.parser.producers.v8.SurvivorAgesInfoProducer;
import com.gcplot.logs.LogsParser;
import com.gcplot.logs.ParserContext;
import com.gcplot.model.Property;
import com.gcplot.model.gc.*;
import com.gcplot.model.gc.GCEvent;
import com.tagtraum.perf.gcviewer.imp.GcLogType;
import com.tagtraum.perf.gcviewer.model.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/24/16
 */
public class GCViewerLogsParser implements LogsParser<ParseResult> {
    protected ConfigurationManager configurationManager;
    protected GCEventFactory eventFactory;
    protected int batchSize = -1;
    protected int threadPoolSize = Runtime.getRuntime().availableProcessors() * 8;
    protected ExecutorService executor;

    public void init() {
        executor = Executors.newFixedThreadPool(threadPoolSize);
    }

    public void destroy() {
        executor.shutdownNow();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}
    }

    @Override
    public ParseResult parse(InputStream reader, Consumer<GCEvent> firstEventListener,
                             Consumer<GCEvent> lastEventListener, Consumer<GCEvent> eventsConsumer,
                             ParserContext ctx) {
        SurvivorAgesInfoProducer agesInfoProducer = new SurvivorAgesInfoProducer();
        MetadataInfoProducer metadataInfoProducer = new MetadataInfoProducer();
        GCResource gcResource = new GCResource("default");
        gcResource.setLogger(ctx.logger());
        StreamDataReader dr;
        final DateTime now = DateTime.now(DateTimeZone.UTC).withDayOfYear(1).withTimeAtStartOfDay();
        GCEvent[] firstEvent = new GCEvent[1];
        AbstractGCEvent<?>[] lastEvent = new AbstractGCEvent[1];
        List<Future> fs = new ArrayList<>();
        Consumer<List<AbstractGCEvent<?>>> c = l -> {
            if (l.size() > 0) {
                if (firstEvent[0] == null) {
                    List<GCEvent> map = map(now, ctx, l.get(0));
                    if (map.size() > 0) {
                        firstEvent[0] = map.get(0);
                        firstEventListener.accept(firstEvent[0]);
                    }
                }
                lastEvent[0] = l.get(l.size() - 1);
                fs.add(executor.submit(() -> {
                    try {
                        l.forEach(e -> map(now, ctx, e).forEach(eventsConsumer));
                    } catch (Throwable t) {
                        ctx.logger().error(t.getMessage(), t);
                    }
                }));
            }
        };
        try {
            if (ctx.collectorType() == GarbageCollectorType.ORACLE_G1) {
                dr = new HotSpotG1DataReader(c, batchSize, gcResource, reader, fetchLogType(ctx));
            } else {
                dr = new HotSpotDataReader(c, batchSize, gcResource, reader, fetchLogType(ctx));
            }
        } catch (UnsupportedEncodingException e) {
            return ParseResult.failure(e);
        }
        dr.excludedHandler(agesInfoProducer::parse);
        dr.headerHandler(metadataInfoProducer::parse);
        StreamGCModel model;
        try {
            model = dr.readStream().finish();
        } catch (IOException e) {
            return ParseResult.failure(e);
        }

        fs.forEach(f -> {
            try {
                f.get();
            } catch (Throwable ignored) {}
        });
        if (lastEvent[0] != null) {
            List<GCEvent> mapped = map(now, ctx, lastEvent[0]);
            lastEventListener.accept(mapped.get(mapped.size() - 1));
        }
        // temp stuff
        return ParseResult.success(Collections.emptyList(),
                Collections.singletonList(agesInfoProducer.averageAgesState()),
                metadataInfoProducer.getLogMetadata(), firstEvent[0]);
    }

    private GcLogType fetchLogType(ParserContext ctx) {
        switch (ctx.vmVersion()) {
            case HOTSPOT_1_2_2: return GcLogType.SUN1_2_2;
            case HOTSPOT_1_3_1: return GcLogType.SUN1_3_1;
            case HOTSPOT_1_4: return GcLogType.SUN1_4;
            case HOTSPOT_1_5: return GcLogType.SUN1_5;
            case HOTSPOT_1_6: if (ctx.collectorType() == GarbageCollectorType.ORACLE_G1) {
                return GcLogType.SUN1_6G1;
            } else {
                return GcLogType.SUN1_6;
            }
            case HOTSPOT_1_7: if (ctx.collectorType() == GarbageCollectorType.ORACLE_G1) {
                return GcLogType.SUN1_7G1;
            } else {
                return GcLogType.SUN1_7;
            }
            case HOTSPOT_1_8:
            case HOTSPOT_1_9: if (ctx.collectorType() == GarbageCollectorType.ORACLE_G1) {
                return GcLogType.SUN1_8G1;
            } else {
                return GcLogType.SUN1_8;
            }
            default: throw new RuntimeException("Unsupported VM version " + ctx.vmVersion());
        }
    }

    public List<GCEvent> map(DateTime now, ParserContext ctx, AbstractGCEvent<?> event) {
        ArrayList<GCEvent> events = new ArrayList<>(1);
        String description = event.getTypeAsString();
        VMEventType vmEventType = VMEventType.GARBAGE_COLLECTION;
        if (event.isVmEvent()) {
            vmEventType = VMEventType.STW_NON_GC;
        }
        Capacity capacity = Capacity.NONE;
        Capacity totalCapacity = Capacity.NONE;
        EventConcurrency concurrency = event.isConcurrent() ? EventConcurrency.CONCURRENT : EventConcurrency.SERIAL;
        EnumSet<Generation> generations = EnumSet.noneOf(Generation.class);
        DateTime datestamp;
        Map<Generation, Capacity> capacityByGeneration = Collections.emptyMap();
        if (event.getDatestamp() == null) {
            datestamp = now.plusMillis((int)(event.getTimestamp() * 1000));
        } else {
            TimeZone timeZone = TimeZone.getTimeZone(event.getDatestamp().getZone().getId());
            DateTimeZone z = DateTimeZone.forTimeZone(timeZone);
            datestamp = new DateTime(new DateTime(event.getDatestamp().toInstant().toEpochMilli(), z), DateTimeZone.UTC);
        }
        double pause = event.getPause();
        if (event.isConcurrent()) {
            ConcurrentGCEvent concurrentGCEvent = (ConcurrentGCEvent) event;
            if (concurrentGCEvent.getDuration() > 0) {
                generations.add(Generation.TENURED);
                pause = concurrentGCEvent.getDuration();
            } else {
                return Collections.emptyList();
            }
        } else if (event.isVmEvent()) {
            generations = EnumSet.of(Generation.OTHER);
        } else {
            com.tagtraum.perf.gcviewer.model.GCEvent gcEvent = (com.tagtraum.perf.gcviewer.model.GCEvent) event;
            totalCapacity = of(gcEvent);
            if (event.getGeneration() == AbstractGCEvent.Generation.YOUNG) {
                if (gcEvent.getYoung() != null) {
                    capacity = of(gcEvent.getYoung());
                }
                generations.add(Generation.YOUNG);
            } else if (event.getGeneration() == AbstractGCEvent.Generation.TENURED) {
                generations.add(Generation.TENURED);
                if (gcEvent.getTenured() != null) {
                    capacity = of(gcEvent.getTenured());
                }
                if (ctx.collectorType() == GarbageCollectorType.ORACLE_G1) {
                    if (gcEvent.getLastYoung() != null && capacity.equals(Capacity.NONE)
                            && !totalCapacity.equals(Capacity.NONE)) {
                        Capacity yc = of(gcEvent.getLastYoung().getYoung());
                        Capacity tyc = of(gcEvent.getLastYoung());

                        // the tenured used after last Young GC is the used-start for Tenured GC
                        long usedBefore = tyc.usedAfter() - yc.usedAfter();
                        long usedAfter = usedBefore - (totalCapacity.usedBefore() - totalCapacity.usedAfter());
                        long total = totalCapacity.total() - yc.total();
                        capacity = Capacity.of(usedBefore, usedAfter, total);
                    }
                }
            } else if (event.getGeneration() == AbstractGCEvent.Generation.ALL) {
                if (capacityByGeneration.equals(Collections.emptyMap())) {
                    capacityByGeneration = new IdentityHashMap<>(2);
                }
                capacity = of(gcEvent);

                if (gcEvent.getYoung() != null) {
                    capacityByGeneration.put(Generation.YOUNG, of(gcEvent.getYoung()));
                    generations.add(Generation.YOUNG);
                }
                if (gcEvent.getTenured() != null) {
                    capacityByGeneration.put(Generation.TENURED, of(gcEvent.getTenured()));
                    generations.add(Generation.TENURED);
                }
                if (gcEvent.getPerm() != null) {
                    Generation g = metaspaceGeneration(gcEvent.getPerm().getTypeAsString());
                    capacityByGeneration.put(g, of(gcEvent.getPerm()));
                    generations.add(g);
                }
            } else if (event.getGeneration() == AbstractGCEvent.Generation.PERM) {
                generations.add(metaspaceGeneration(gcEvent.getPerm().getTypeAsString()));
                if (gcEvent.getPerm() != null) {
                    capacity = of(gcEvent.getPerm());
                }
            } else if (event.getGeneration() == AbstractGCEvent.Generation.OTHER) {
                ctx.logger().warn("Strangely, an event is considered OTHER: {}", event);
            }
        }
        Phase phase = detectPhase(ctx, event);
        Cause cause = detectCause(event);
        long properties = detectProperties(event);
        events.add(eventFactory.create(null, null, ctx.streamChecksum(), datestamp, description, vmEventType, capacity, totalCapacity,
                event.getTimestamp(), (long)(pause * 1_000_000), generations, phase, cause, properties, concurrency, capacityByGeneration, ""));

        return events;
    }

    private long detectProperties(AbstractGCEvent<?> event) {
        String type = event.getTypeAsString().trim();
        long properties = 0;
        if (type.endsWith("(mixed)")) {
            properties |= Property.G1_MIXED;
        }
        return properties;
    }

    private Cause detectCause(AbstractGCEvent<?> event) {
        String type = event.getTypeAsString();
        if (type.contains("Allocation Failure")) {
            return Cause.ALLOCATION_FAILURE;
        } else if (type.contains("G1 Evacuation Pause")) {
            return Cause.G1_EVACUATION_PAUSE;
        } else if (type.contains("GCLocker Initiated GC")) {
            return Cause.GC_LOCKER;
        } else if (type.contains("System.gc()")) {
            return Cause.SYSTEM_GC;
        } else if (type.contains("Allocation Profiler")) {
            return Cause.ALLOCATION_PROFILER;
        } else if (type.contains("Metadata GC Threshold")) {
            return Cause.METADATA_GC_THRESHOLD;
        } else if (type.contains("Permanent Generation Full")) {
            return Cause.PERM_GENERATION_FULL;
        } else if (type.contains("Heap Inspection Initiated GC")) {
            return Cause.HEAP_INSPECTION;
        } else if (type.contains("Heap Dump Initiated GC")) {
            return Cause.HEAP_DUMP;
        } else if (type.contains("No GC")) {
            return Cause.NO_GC;
        } else if (type.contains("Ergonomics")) {
            return Cause.ADAPTIVE_SIZE_ERGONOMICS;
        } else if (type.contains("G1 Humongous Allocation")) {
            return Cause.G1_HUMONGOUS_ALLOCATION;
        } else if (type.contains("CMS Initial Mark")) {
            return Cause.CMS_INITIAL_MARK;
        } else if (type.contains("CMS Final Remark")) {
            return Cause.CMS_FINAL_REMARK;
        } else if (type.contains("Last ditch collection")) {
            return Cause.LAST_DITCH_COLLECTION;
        } else if (type.contains("JvmtiEnv ForceGarbageCollection")) {
            return Cause.JVMTI_ENV;
        } else {
            return Cause.OTHER;
        }
    }

    private Phase detectPhase(ParserContext ctx, AbstractGCEvent<?> event) {
        String type = event.getTypeAsString();
        if (ctx.collectorType() == GarbageCollectorType.ORACLE_G1) {
            if (type.contains("GC pause") && (type.contains("(young)") || type.contains("(mixed)"))) {
                return Phase.G1_COPYING;
            } else if (type.contains("(initial-mark)")) {
                return Phase.G1_INITIAL_MARK;
            } else if (event.isConcurrent() && type.contains("root-region-scan")) {
                return Phase.G1_ROOT_REGION_SCANNING;
            } else if (event.isConcurrent() && type.contains("concurrent-mark")) {
                return Phase.G1_CONCURRENT_MARKING;
            } else if (type.contains("GC remark")) {
                return Phase.G1_REMARK;
            } else if (event.isConcurrent() && type.contains("concurrent-cleanup")) {
                return Phase.G1_CLEANUP;
            }
        } else {
            if (type.contains("CMS-initial-mark")) {
                return Phase.CMS_INITIAL_MARK;
            } else if (event.isConcurrent() && type.contains("CMS-concurrent-mark")) {
                return Phase.CMS_CONCURRENT_MARK;
            } else if (event.isConcurrent() && type.contains("CMS-concurrent") && type.contains("preclean")) {
                return Phase.CMS_CONCURRENT_PRECLEAN;
            } else if (type.contains("CMS-remark")) {
                return Phase.CMS_REMARK;
            } else if (event.isConcurrent() && type.contains("CMS-concurrent-sweep")) {
                return Phase.CMS_CONCURRENT_SWEEP;
            } else if (event.isConcurrent() && type.contains("CMS-concurrent-reset")) {
                return Phase.CMS_CONCURRENT_RESET;
            }
        }
        return Phase.OTHER;
    }

    protected Generation metaspaceGeneration(String type) {
        Generation g;
        if (type.toLowerCase().contains("metaspace")) {
            g = Generation.METASPACE;
        } else {
            g = Generation.PERM;
        }
        return g;
    }

    protected Capacity of(com.tagtraum.perf.gcviewer.model.GCEvent gcEvent) {
        return Capacity.of(gcEvent.getPreUsed(), gcEvent.getPostUsed(), gcEvent.getTotal());
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public void setEventFactory(GCEventFactory eventFactory) {
        this.eventFactory = eventFactory;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }
}
