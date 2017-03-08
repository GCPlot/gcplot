package com.gcplot.controllers.gc;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import com.gcplot.Identifier;
import com.gcplot.analytics.AnalyticsService;
import com.gcplot.analytics.EventsResult;
import com.gcplot.analytics.GCEventFeature;
import com.gcplot.commons.*;
import com.gcplot.commons.exceptions.Exceptions;
import com.gcplot.commons.serialization.JsonSerializer;
import com.gcplot.controllers.Controller;
import com.gcplot.log_processor.LogMetadata;
import com.gcplot.log_processor.parser.ParseResult;
import com.gcplot.log_processor.parser.detect.VMProperties;
import com.gcplot.log_processor.parser.detect.VMPropertiesDetector;
import com.gcplot.log_processor.survivor.AgesState;
import com.gcplot.logs.LogsParser;
import com.gcplot.logs.ParserContext;
import com.gcplot.messages.GCEventResponse;
import com.gcplot.messages.GCRateResponse;
import com.gcplot.model.gc.*;
import com.gcplot.repository.GCAnalyseRepository;
import com.gcplot.repository.GCEventRepository;
import com.gcplot.repository.VMEventsRepository;
import com.gcplot.repository.operations.analyse.AddJvmOperation;
import com.gcplot.repository.operations.analyse.AnalyseOperation;
import com.gcplot.repository.operations.analyse.UpdateCornerEventsOperation;
import com.gcplot.repository.operations.analyse.UpdateJvmInfoOperation;
import com.gcplot.resources.ResourceManager;
import com.gcplot.services.stats.StatisticAggregateInterceptor;
import com.gcplot.web.RequestContext;
import com.gcplot.web.UploadedFile;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.joda.time.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;

import static com.gcplot.commons.CollectionUtils.cloneAndAdd;
import static com.gcplot.commons.CollectionUtils.cloneAndPut;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/13/16
 */
public class EventsController extends Controller {
    public static final String DEFAULT_CHUNK_DELIMETER = "$d";
    public static final String ANONYMOUS_ANALYSE_ID = "7acada7b-e109-4d11-ac01-b3521d9d58c3";
    protected static final Logger LOG = LoggerFactory.getLogger(EventsController.class);
    private static final String LOG_PATTERN = "%d{yyyyMMdd HH:mm:ss.SSS} [[%5p] %c{1} [%t]] %m%n";
    private static final String ANONYMOUS_ANALYSE_NAME = "Files";
    private static final int ERASE_ALL_PERIOD_YEARS = 10;
    private static final EnumSet<Generation> OTHER_GENERATION = EnumSet.of(Generation.OTHER);
    private ThreadLocal<ch.qos.logback.classic.Logger> loggers = new ThreadLocal<ch.qos.logback.classic.Logger>() {
        @Override
        protected ch.qos.logback.classic.Logger initialValue() {
            return ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Thread.currentThread().getName());
        }
    };
    private ExecutorService uploadExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 8);
    private Cache<Triple, StatisticAggregateInterceptor> analyseStatsCache;
    @Autowired
    private GCAnalyseRepository analyseRepository;
    @Autowired
    private GCEventRepository eventRepository;
    @Autowired
    private VMEventsRepository<ObjectsAges> agesStateRepository;
    @Autowired
    private LogsParser<ParseResult> logsParser;
    @Autowired
    private ResourceManager resourceManager;
    @Autowired
    private AnalyticsService analyticsService;

    @PostConstruct
    public void init() {
        analyseStatsCache = Caffeine.newBuilder()
                .maximumSize(config.readLong(ConfigProperty.ANALYSIS_STATISTIC_CACHE_SIZE))
                .expireAfterWrite(config.readLong(ConfigProperty.ANALYSIS_STATISTIC_CACHE_SECONDS), TimeUnit.SECONDS)
                .build();
        dispatcher.requireAuth().blocking().filter(c ->
                c.files().size() == 1,
                "You should provide only a single log file.")
                .postUpload("/gc/jvm/log/process", this::processJvmLog);
        dispatcher.blocking().requireAuth().filter(requiredWithPeriod(), periodMessage())
                .get("/gc/jvm/events", this::jvmEvents);
        dispatcher.blocking().requireAuth().filter(requiredWithPeriod(), periodMessage())
                .get("/gc/jvm/events/stream", this::jvmEventsStream);
        dispatcher.blocking().requireAuth().filter(requiredWithPeriod(), periodMessage())
                .get("/gc/jvm/events/full/stream", this::fullJvmEventsStream);
        dispatcher.blocking().requireAuth().filter(requiredWithPeriod(), periodMessage())
                .get("/gc/jvm/events/full/sample/stream", this::fullJvmSampleEventsStream);
        dispatcher.requireAuth().filter(requiredWithPeriod(), periodMessage())
                .get("/gc/jvm/events/erase", this::jvmEventsErase);
        dispatcher.requireAuth().filter(requiredWithoutPeriod(), message())
                .get("/gc/jvm/events/erase/all", this::jvmEventsEraseAll);
        dispatcher.requireAuth().filter(requiredWithPeriod(), message())
                .get("/gc/jvm/events/stats", this::jvmStats);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        try {
            uploadExecutor.shutdownNow();
            uploadExecutor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
    }

    /**
     * POST /gc/jvm/log/process
     * Require Auth (token)
     * Body: Single file upload
     */
    public void processJvmLog(RequestContext ctx) {
        UploadedFile uf = ctx.files().get(0);
        final boolean isSync = Boolean.parseBoolean(ctx.param("sync", "false"));
        final String analyseId = ctx.param("analyse_id", ANONYMOUS_ANALYSE_ID);
        final String jvmId = ctx.param("jvm_id", UUID.randomUUID().toString());
        final String username = esc(account(ctx).username());
        try {
            final Identifier userId = account(ctx).id();
            String checksum = ctx.param("checksum", null);

            Optional<GCAnalyse> oAnalyse = Optional.empty();
            if (Objects.equals(analyseId, ANONYMOUS_ANALYSE_ID)) {
                oAnalyse = analyseRepository.analyse(userId, analyseId);
                if (!oAnalyse.isPresent()) {
                    GCAnalyseImpl analyse = new GCAnalyseImpl().name(ANONYMOUS_ANALYSE_NAME).accountId(userId).id(analyseId)
                            .isContinuous(false).start(DateTime.now(DateTimeZone.UTC)).ext("");
                    analyseRepository.newAnalyse(analyse);
                    oAnalyse = Optional.of(analyse);
                }
                if (!oAnalyse.get().jvmIds().contains(jvmId)) {
                    VMProperties props = VMProperties.EMPTY;
                    try (InputStream fis = logStream(uf)) {
                        props = VMPropertiesDetector.detect(fis);
                    } catch (Throwable ignored) {
                    }
                    if (props == VMProperties.EMPTY) {
                        ctx.write(ErrorMessages.buildJson(ErrorMessages.LOG_FILE_UNDETECTABLE));
                        return;
                    }

                    analyseRepository.perform(new AddJvmOperation(userId, analyseId, jvmId, uf.originalName(),
                            props.getVersion(), props.getGcType(), "", null));
                    GCAnalyseImpl newAnalyse = new GCAnalyseImpl(oAnalyse.get());
                    newAnalyse.jvmIds(cloneAndAdd(newAnalyse.jvmIds(), jvmId));
                    newAnalyse.jvmNames(cloneAndPut(newAnalyse.jvmNames(), jvmId, uf.originalName()));
                    newAnalyse.jvmVersions(cloneAndPut(newAnalyse.jvmVersions(), jvmId, props.getVersion()));
                    newAnalyse.jvmGCTypes(cloneAndPut(newAnalyse.jvmGCTypes(), jvmId, props.getGcType()));
                    oAnalyse = Optional.of(newAnalyse);
                }
            }
            GCAnalyse analyse = oAnalyse.orElse(analyseRepository.analyse(userId, analyseId).orElse(null));
            if (!isAnalyseIncorrect(ctx, analyseId, jvmId, analyse)) {
                try {
                    final File logFile = java.nio.file.Files.createTempFile("gcplot_log", ".log").toFile();
                    if (checksum == null) {
                        checksum = calculateFileChecksum(ctx, uf);
                    }
                    Logger log = createLogger(logFile);
                    AtomicReference<DateTime> lastEventTime = new AtomicReference<>(null);
                    ParseResult pr = parseAndPersist(isSync, uf, jvmId, checksum, analyse, log, e -> {
                        if (e.generations().size() > 0 && !e.generations().equals(OTHER_GENERATION) &&
                                (lastEventTime.get() == null || lastEventTime.get().isBefore(e.occurred()))) {
                            lastEventTime.set(e.occurred());
                        }
                        e.analyseId(analyseId);
                        e.jvmId(jvmId);
                    });
                    if (!pr.isSuccessful()) {
                        LOG.debug(pr.getException().get().getMessage(), pr.getException().get());
                        log.error(pr.getException().get().getMessage(), pr.getException().get());
                    } else {
                        List<AnalyseOperation> ops = new ArrayList<>(2);
                        if (lastEventTime.get() != null) {
                            ops.add(new UpdateCornerEventsOperation(userId, analyseId, jvmId,
                                    pr.getFirstEvent() != null ? pr.getFirstEvent().occurred() : null,
                                    lastEventTime.get()));
                        }
                        if (pr.getLogMetadata().isPresent()) {
                            updateAnalyseMetadata(analyseId, jvmId, userId, pr, ops);
                        }
                        if (ops.size() > 0) {
                            analyseRepository.perform(ops);
                        }
                        if (pr.getAgesStates().size() > 0) {
                            persistObjectAges(analyseId, jvmId, pr);
                        }
                    }

                    truncateFile(logFile, getConfig().readLong(ConfigProperty.PARSE_LOG_MAX_FILE_SIZE));
                    uploadLogFile(isSync, analyseId, jvmId, username, logFile);
                    ctx.response(SUCCESS);
                } catch (Throwable t) {
                    throw Exceptions.runtime(t);
                }
            }
        } finally {
            uploadLogFile(isSync, "gc-log", analyseId, jvmId, username, uf.file());
            // TODO log file size processed per user
        }
    }

    /**
     * GET /gc/jvm/events
     * Require Auth (token)
     * Params:
     *  - analyse_id, string (required)
     *  - jvm_id, string (required)
     *  - from, timestamp (required)
     *  - to, timestamp (required)
     *  - tz, string (optional)
     */
    public void jvmEvents(RequestContext ctx) {
        PeriodParams pp = new PeriodParams(ctx);

        checkPeriodAndExecute(pp, ctx, () -> {
            ctx.setChunked(false);
            List<GCEvent> events = eventRepository.pauseEvents(pp.getAnalyseId(), pp.getJvmId(), Range.of(pp.getInterval()));
            ctx.response(GCEventResponse.from(events));
        });
    }

    /**
     * GET /gc/jvm/events
     * Require Auth (token)
     * Params:
     *  - analyse_id, string (required)
     *  - jvm_id, string (required)
     *  - from, timestamp (required)
     *  - to, timestamp (required)
     *  - tz, string (optional)
     */
    public void jvmEventsStream(RequestContext ctx) {
        PeriodParams pp = new PeriodParams(ctx);

        checkPeriodAndExecute(pp, ctx, () -> {
            ctx.setChunked(true);
            Iterator<GCEvent> eventIterator = eventRepository.lazyPauseEvents(pp.getAnalyseId(), pp.getJvmId(),
                    Range.of(pp.getInterval()));
            streamEvents(ctx, pp, eventIterator);
            ctx.finish();
        });
    }

    /**
     * GET /gc/jvm/events/full/stream
     */
    public void fullJvmEventsStream(RequestContext ctx) {
        PeriodParams pp = new PeriodParams(ctx);

        checkPeriodAndExecute(pp, ctx, () -> {
            ctx.setChunked(true);
            Iterator<GCEvent> eventIterator = eventRepository.lazyEvents(pp.getAnalyseId(), pp.getJvmId(),
                    Range.of(pp.getInterval()));
            streamEvents(ctx, pp, eventIterator);
            ctx.finish();
        });
    }

    /**
     * GET /gc/jvm/events/full/sample/stream
     */
    public void fullJvmSampleEventsStream(RequestContext ctx) {
        PeriodParams pp = new PeriodParams(ctx);

        checkPeriodAndExecute(pp, ctx, () -> {
            ctx.setChunked(true);
            EnumSet<GCEventFeature> features = pp.isStats() ? GCEventFeature.getAll() : GCEventFeature.getSamplers();
            StatisticAggregateInterceptor cached = analyseStatsCache.get(statsKey(pp), k -> null);
            if (cached != null) {
                features = EnumSet.copyOf(features);
                features.remove(GCEventFeature.CALC_STATISTIC);
            }

            EventsResult r = analyticsService.events(account(ctx).id(), pp.getAnalyseId(), pp.getJvmId(), pp.getInterval(),
                    features, e -> {
                        if (e.isGCEvent()) {
                            ctx.write(GCEventResponse.toJson((GCEvent) e));
                        } else if (e.isRate()) {
                            ctx.write(GCRateResponse.toJson((GCRate) e));
                        } else if (e.isStatistic()) {
                            analyseStatsCache.put(statsKey(pp), (StatisticAggregateInterceptor) e);
                            ctx.write(JsonSerializer.serialize(e));
                        }
                        delimit(ctx, pp);
                    });
            if (!r.isSuccess()) {
                ctx.write(r.getErrorMessage());
                delimit(ctx, pp);
            } else if (cached != null) {
                ctx.write(JsonSerializer.serialize(cached));
                delimit(ctx, pp);
            }
        });
    }

    private Triple statsKey(PeriodParams pp) {
        return Triple.of(Pair.of(pp.getInterval().getStart().getMillis(), pp.getInterval().getEnd().getMillis()),
                pp.getAnalyseId(), pp.getJvmId());
    }

    /**
     * GET /gc/jvm/events/erase
     * Require Auth (token)
     * Params:
     *  - analyse_id, string (required)
     *  - jvm_id, string (required)
     *  - from, timestamp (required)
     *  - to, timestamp (required)
     *  - tz, string (optional)
     */
    public void jvmEventsErase(RequestContext ctx) {
        PeriodParams pp = new PeriodParams(ctx);

        Optional<GCAnalyse> analyse = analyseRepository.analyse(account(ctx).id(), pp.getAnalyseId());
        analyse.ifPresent(gcAnalyse ->
                eventRepository.erase(pp.getAnalyseId(), pp.getJvmId(), Range.of(pp.getInterval())));
        ctx.response(SUCCESS);
    }

    /**
     * GET /gc/jvm/events/erase/all
     * Require Auth (token)
     * Params:
     *  - analyse_id, string (required)
     *  - jvm_id, string (required)
     */
    public void jvmEventsEraseAll(RequestContext ctx) {
        final String analyseId = ctx.param("analyse_id");
        final String jvmId = ctx.param("jvm_id");

        Optional<GCAnalyse> analyse = analyseRepository.analyse(account(ctx).id(), analyseId);
        if (analyse.isPresent()) {
            DateTime lastEvent = analyse.get().lastEvent().get(jvmId);

            if (lastEvent != null) {
                eventRepository.erase(analyseId, jvmId, Range.of(lastEvent.minusYears(ERASE_ALL_PERIOD_YEARS), lastEvent.plusDays(1)));
            }
        }
        ctx.response(SUCCESS);
    }

    /**
     * GET /gc/jvm/events/stats
     */
    public void jvmStats(RequestContext ctx) {
        PeriodParams pp = new PeriodParams(ctx);

        checkPeriodAndExecute(pp, ctx, () -> {
            EventsResult r = analyticsService.events(account(ctx).id(), pp.getAnalyseId(), pp.getJvmId(), pp.getInterval(),
                    EnumSet.of(GCEventFeature.CALC_STATISTIC), e -> {
                        if (e.isStatistic()) {
                            ctx.write(JsonSerializer.serialize(e));
                        }
                    });
            if (!r.isSuccess()) {
                ctx.write(r.getErrorMessage());
            }
        });
    }

    private void streamEvents(RequestContext ctx, PeriodParams pp, Iterator<GCEvent> eventIterator) {
        while (eventIterator.hasNext()) {
            GCEvent event = eventIterator.next();
            if (event != null) {
                write(ctx, pp, event);
            }
        }
    }

    private void write(RequestContext ctx, PeriodParams pp, GCEvent event) {
        if (!event.generations().equals(OTHER_GENERATION)) {
            ctx.write(GCEventResponse.toJson(event));
            delimit(ctx, pp);
        }
    }

    private void delimit(RequestContext ctx, PeriodParams pp) {
        if (pp.isDelimit()) {
            ctx.write(DEFAULT_CHUNK_DELIMETER);
        }
    }

    private void checkPeriodAndExecute(PeriodParams pp, RequestContext ctx, Runnable ex) {
        if (Days.daysBetween(pp.getInterval().getStart(), pp.getInterval().getEnd()).getDays() >
                config.readInt(ConfigProperty.GC_EVENTS_MAX_INTERVAL_DAYS)) {
            ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM, "Days interval exceeds max "
                    + config.readInt(ConfigProperty.GC_EVENTS_MAX_INTERVAL_DAYS) + " days."));
        } else if (Minutes.minutesBetween(pp.getInterval().getStart(), pp.getInterval().getEnd()).getMinutes() < 1) {
            ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM,
                    "The interval should higher or equal to 1 minute."));
        } else {
            ex.run();
        }
    }

    private void truncateFile(File logFile, long maxSize) {
        if (logFile.length() > maxSize) {
            try (FileChannel outChan = new FileOutputStream(logFile, true).getChannel()) {
                outChan.truncate(maxSize);
            } catch (Throwable t) {
                LOG.error(t.getMessage(), t);
            }
        }
    }

    protected boolean isG1(PeriodParams pp, GCAnalyse analyse) {
        return analyse.jvmGCTypes().get(pp.jvmId) == GarbageCollectorType.ORACLE_G1;
    }

    private String calculateFileChecksum(RequestContext ctx, UploadedFile uf) throws IOException, NoSuchAlgorithmException {
        String checksum;
        LOG.warn("No checksum was provided in the request, calculating own: {}", ctx);
        checksum = FileUtils.getFileChecksum(MessageDigest.getInstance("MD5"), uf.file());
        LOG.warn("The calculated checksum for {} is {}", uf.fileName(), checksum);
        return checksum;
    }

    private void updateAnalyseMetadata(String analyseId, String jvmId, Identifier userId, ParseResult pr,
                                       List<AnalyseOperation> ops) {
        LogMetadata md = pr.getLogMetadata().get();
        ops.add(new UpdateJvmInfoOperation(userId, analyseId, jvmId, md.commandLines(),
                new MemoryDetailsImpl(md.pageSize(), md.physicalTotal(), md.physicalFree(),
                        md.swapTotal(), md.swapFree())));
    }

    private void persistObjectAges(String analyseId, String jvmId, ParseResult pr) {
        List<ObjectsAges> oas = new ArrayList<>(1);
        for (AgesState as : pr.getAgesStates()) {
            ObjectsAgesImpl oa = new ObjectsAgesImpl();
            oa.analyseId(analyseId);
            oa.jvmId(jvmId);
            oa.desiredSurvivorSize(as.getDesiredSurvivorSize());
            oa.occurred(DateTime.now(DateTimeZone.UTC));
            oa.occupied(as.getOccupied());
            oa.total(as.getTotal());
            oas.add(oa);
        }
        agesStateRepository.add(oas);
    }

    private void uploadLogFile(boolean isSync, String analyseId, String jvmId, String username, File logFile) {
        uploadLogFile(isSync, "log", analyseId, jvmId, username, logFile);
    }

    private void uploadLogFile(boolean isSync, String rootFolder,
                               String analyseId, String jvmId, String username, File logFile) {
        if (!resourceManager.isDisabled()) {
            Future f = uploadExecutor.submit(() -> {
                try {
                    LOG.debug("Starting uploading {}", logFile);
                    resourceManager.upload(logFile,
                            rootFolder + "/" + username + "/" + esc(analyseId) + "/" + esc(jvmId));
                } catch (Throwable t) {
                    LOG.error(t.getMessage(), t);
                } finally {
                    org.apache.commons.io.FileUtils.deleteQuietly(logFile);
                }
            });
            if (isSync) {
                try {
                    f.get();
                } catch (InterruptedException | ExecutionException ignored) {
                }
            }
        } else {
            LOG.warn("Resource Manager is currently disabled, can't upload log file.");
            org.apache.commons.io.FileUtils.deleteQuietly(logFile);
        }
    }

    private String esc(String username) {
        return username.toLowerCase().replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    private boolean isAnalyseIncorrect(RequestContext ctx, String analyseId, String jvmId, GCAnalyse analyse) {
        if (analyse == null) {
            ctx.write(ErrorMessages.buildJson(ErrorMessages.UNKNOWN_GC_ANALYSE, analyseId));
            return true;
        }
        if (!analyse.jvmIds().contains(jvmId)) {
            ctx.write(ErrorMessages.buildJson(ErrorMessages.UNKNOWN_JVM_ID, jvmId));
            return true;
        }
        return analyseNotInAccount(ctx, analyse);
    }

    private boolean analyseNotInAccount(RequestContext ctx, GCAnalyse analyse) {
        if (!analyse.accountId().equals(account(ctx).id())) {
            ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM, "Analyse doesn't belong to the current account."));
            return true;
        }
        return false;
    }

    private ParseResult parseAndPersist(boolean isSync, UploadedFile uf, String jvmId, String checksum, GCAnalyse analyse,
                                        Logger log, Consumer<GCEventImpl> enricher) throws IOException {
        ParserContext ctx = new ParserContext(log, checksum, analyse.jvmGCTypes().get(jvmId),
                analyse.jvmVersions().get(jvmId));
        final int batchSize = config.readInt(ConfigProperty.BATCH_PUT_GC_EVENT_SIZE);
        final ThreadLocal<List<GCEvent>> es = ThreadLocal.withInitial(() -> new ArrayList<>(batchSize));
        final GCEvent[] firstParsed = new GCEvent[1];
        final boolean forbidOtherGen = config.readBoolean(ConfigProperty.FORBID_OTHER_GENERATION);
        LazyVal<GCEvent> lastPersistedEvent = LazyVal.ofOpt(() ->
                eventRepository.lastEvent(analyse.id(), jvmId, checksum, firstParsed[0].occurred().minusDays(1)));
        ParseResult pr;
        try (InputStream fis = logStream(uf)) {
            pr = logsParser.parse(fis, first -> {
                        if (first.generations().size() > 0 && !first.generations().equals(OTHER_GENERATION)) {
                            firstParsed[0] = first;
                            lastPersistedEvent.get();
                            return true;
                        } else {
                            return false;
                        }
                    }, last -> enricher.accept((GCEventImpl) last),
                    e -> {
                        List<GCEvent> events = es.get();
                        if (e != null) {
                            enricher.accept((GCEventImpl) e);
                            if (firstParsed[0] == null || lastPersistedEvent.get() == null || lastPersistedEvent.get().timestamp() <
                                    e.timestamp() && (!forbidOtherGen || !e.generations().equals(OTHER_GENERATION))) {

                                if (events.size() > 0 && events.size() % batchSize == 0) {
                                    persist(isSync, events);
                                }
                                events.add(e);
                            }
                        } else if (events.size() > 0) {
                            persist(isSync, events);
                        }
                    }, ctx);
        }
        return pr;
    }

    private InputStream logStream(UploadedFile uf) {
        try {
            InputStream fis = new FileInputStream(uf.file());
            if (isGzipped(uf.file())) {
                fis = new GZIPInputStream(fis);
            } else {
                fis = new BufferedInputStream(fis);
            }
            return fis;
        } catch (Throwable t) {
            throw Exceptions.runtime(t);
        }
    }

    private boolean isGzipped(File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file)){
            final int b1 = in.read();
            final int b2 = in.read();
            if (b2 < 0) {
                throw new EOFException();
            }
            int firstBytes = (b2 << 8) | b1;
            return firstBytes == GZIPInputStream.GZIP_MAGIC;
        }
    }

    private void persist(boolean isSync, List<GCEvent> events) {
        if (isSync) {
            eventRepository.add(events);
        } else {
            events.forEach(eventRepository::addAsync);
            //eventRepository.addAsync(events);
        }
        events.clear();
    }

    private Logger createLogger(File logFile) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger log = loggers.get();
        log.setAdditive(false);

        FileAppender fileAppender = new FileAppender();
        fileAppender.setContext(context);
        fileAppender.setName(Thread.currentThread().getName());
        fileAppender.setFile(logFile.getAbsolutePath());

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(LOG_PATTERN);
        encoder.start();

        fileAppender.setEncoder(encoder);
        fileAppender.start();

        log.detachAndStopAllAppenders();
        log.addAppender(fileAppender);

        return log;
    }

    private Predicate<RequestContext> requiredWithoutPeriod() {
        return c -> c.hasParam("analyse_id") && c.hasParam("jvm_id");
    }

    private Predicate<RequestContext> requiredWithPeriod() {
        return c -> c.hasParam("analyse_id") && c.hasParam("jvm_id")
                && c.hasParam("from") && c.hasParam("to");
    }

    private String message() {
        return "Params analyse_id and jvm_id are restricted.";
    }

    private String periodMessage() {
        return "Params required: analyse_id, jvm_id, from, to";
    }

    protected static class PeriodParams {
        private final String analyseId;
        private final String jvmId;
        private final Interval interval;
        private final boolean delimit;
        private final boolean stats;

        public String getAnalyseId() {
            return analyseId;
        }
        public String getJvmId() {
            return jvmId;
        }
        public Interval getInterval() {
            return interval;
        }
        public boolean isDelimit() {
            return delimit;
        }
        public boolean isStats() {
            return stats;
        }

        public PeriodParams(RequestContext ctx) {
            DateTimeZone tz;
            try {
                tz = DateTimeZone.forID(ctx.param("tz", "UTC"));
            } catch (Throwable t) {
                ctx.finish(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM, "Param tz is invalid."));
                throw Exceptions.runtime(t);
            }
            this.analyseId = ctx.param("analyse_id");
            this.jvmId = ctx.param("jvm_id");
            this.interval = new Interval(new DateTime(Long.parseLong(ctx.param("from")), tz),
                    new DateTime(Long.parseLong(ctx.param("to")), tz));
            this.delimit = Boolean.parseBoolean(ctx.param("delimit", "false"));
            this.stats = Boolean.parseBoolean(ctx.param("stats", "false"));
        }
    }
}
