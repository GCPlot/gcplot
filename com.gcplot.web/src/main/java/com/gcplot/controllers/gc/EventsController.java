package com.gcplot.controllers.gc;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import com.gcplot.Identifier;
import com.gcplot.commons.*;
import com.gcplot.commons.exceptions.Exceptions;
import com.gcplot.controllers.Controller;
import com.gcplot.log_processor.LogMetadata;
import com.gcplot.log_processor.parser.ParseResult;
import com.gcplot.log_processor.survivor.AgesState;
import com.gcplot.logs.LogsParser;
import com.gcplot.logs.ParserContext;
import com.gcplot.messages.GCEventResponse;
import com.gcplot.model.gc.*;
import com.gcplot.repository.GCAnalyseRepository;
import com.gcplot.repository.GCEventRepository;
import com.gcplot.repository.VMEventsRepository;
import com.gcplot.repository.operations.analyse.UpdateJvmInfoOperation;
import com.gcplot.repository.operations.analyse.UpdateLastEventOperation;
import com.gcplot.resources.ResourceManager;
import com.gcplot.web.RequestContext;
import com.gcplot.web.UploadedFile;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.omg.CORBA.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/13/16
 */
public class EventsController extends Controller {
    private static final int ERASE_ALL_PERIOD_YEARS = 10;
    protected static final Logger LOG = LoggerFactory.getLogger(EventsController.class);
    protected static final String LOG_PATTERN = "%d{yyyyMMdd HH:mm:ss.SSS} [[%5p] %c{1} [%t]] %m%n";

    @PostConstruct
    public void init() {
        dispatcher.requireAuth().blocking().filter(c ->
                c.files().size() == 1,
                "You should provide only a single log file.")
                .filter(requiredWithoutPeriod(), message())
                .post("/gc/jvm/log/process", this::processJvmLog);
        dispatcher.requireAuth().filter(requiredWithPeriod(), periodMessage())
                .get("/gc/jvm/events", this::jvmEvents);
        dispatcher.requireAuth().filter(requiredWithPeriod(), periodMessage())
                .get("/gc/jvm/events/stream", this::jvmEventsStream);
        dispatcher.requireAuth().filter(requiredWithPeriod(), periodMessage())
                .get("/gc/jvm/events/erase", this::jvmEventsErase);
        dispatcher.requireAuth().filter(requiredWithoutPeriod(), message())
                .get("/gc/jvm/events/erase/all", this::jvmEventsEraseAll);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        try {
            executor.shutdownNow();
            executor.awaitTermination(1, TimeUnit.MINUTES);
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
        final String analyseId = ctx.param("analyse_id");
        final String jvmId = ctx.param("jvm_id");
        final Identifier userId = account(ctx).id();
        final String username = esc(account(ctx).username());
        String checksum = ctx.param("checksum", null);

        Optional<GCAnalyse> analyse = analyseRepository.analyse(analyseId);
        if (isAnalyseIncorrect(ctx, analyseId, jvmId, analyse.orElse(null))) return;
        try {
            final File logFile = java.nio.file.Files.createTempFile("gcplot_log", ".log").toFile();
            if (checksum == null) {
                checksum = calculateFileChecksum(ctx, uf);
            }
            Logger log = createLogger(logFile);
            DateTime[] lastEventTime = new DateTime[1];
            ParseResult pr = parseAndPersist(uf, jvmId, checksum, analyse.get(), log, e -> {
                lastEventTime[0] = e.occurred();
                e.analyseId(analyseId);
                e.jvmId(jvmId);
            });
            // TODO perform operation in the single batch
            if (!pr.isSuccessful()) {
                LOG.debug(pr.getException().get().getMessage(), pr.getException().get());
                log.error(pr.getException().get().getMessage(), pr.getException().get());
            } else {
                if (lastEventTime[0] != null) {
                    analyseRepository.perform(new UpdateLastEventOperation(userId, analyseId, lastEventTime[0]));
                }
                if (pr.getLogMetadata().isPresent()) {
                    updateAnalyseMetadata(analyseId, jvmId, userId, pr);
                }
                if (pr.getAgesStates().size() > 0) {
                    persistObjectAges(analyseId, jvmId, pr);
                }
            }
            uploadLogFile(isSync, analyseId, jvmId, username, logFile);
            ctx.response(SUCCESS);
        } catch (Throwable t) {
            throw Exceptions.runtime(t);
        } finally {
            // TODO log file size processed per user
            FileUtils.deleteSilent(uf.file());
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

        if (Days.daysBetween(pp.getFrom(), pp.getTo()).getDays() > config.readInt(ConfigProperty.GC_EVENTS_MAX_INTERVAL_DAYS)) {
            ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM, "Days interval exceeds max "
                    + config.readInt(ConfigProperty.GC_EVENTS_MAX_INTERVAL_DAYS) + " days."));
        } else {
            ctx.setChunked(false);
            List<GCEvent> events = eventRepository.pauseEvents(pp.getAnalyseId(), pp.getJvmId(), Range.of(pp.getFrom(), pp.getTo()));
            ctx.response(GCEventResponse.from(events, pp.getTimeZone()));
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
    public void jvmEventsStream(RequestContext ctx) {
        PeriodParams pp = new PeriodParams(ctx);

        if (Days.daysBetween(pp.getFrom(), pp.getTo()).getDays() > config.readInt(ConfigProperty.GC_EVENTS_MAX_INTERVAL_DAYS)) {
            ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM, "Days interval exceeds max "
                    + config.readInt(ConfigProperty.GC_EVENTS_MAX_INTERVAL_DAYS) + " days."));
        } else {
            ctx.setChunked(true);
            Iterator<GCEvent> eventIterator = eventRepository.lazyPauseEvents(pp.getAnalyseId(), pp.getJvmId(),
                    Range.of(pp.getFrom(), pp.getTo()));
            while (eventIterator.hasNext()) {
                ctx.response(GCEventResponse.from(eventIterator.next(), pp.getTimeZone()));
            }
            ctx.finish();
        }
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

        if (!checkAnalyseBelongsToAccount(ctx, pp.getAnalyseId()).getLeft()) return;
        eventRepository.erase(pp.getAnalyseId(), pp.getJvmId(), Range.of(pp.getFrom(), pp.getTo()));
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

        Pair<Boolean, GCAnalyse> r = checkAnalyseBelongsToAccount(ctx, analyseId);
        if (!r.getLeft()) return;
        DateTime lastEvent = r.getRight().lastEvent();

        if (lastEvent != null) {
            eventRepository.erase(analyseId, jvmId, Range.of(lastEvent.minusYears(ERASE_ALL_PERIOD_YEARS), lastEvent.plusDays(1)));
        }
        ctx.response(SUCCESS);
    }

    private String calculateFileChecksum(RequestContext ctx, UploadedFile uf) throws IOException, NoSuchAlgorithmException {
        String checksum;
        LOG.warn("No checksum was provided in the request, calculating own: {}", ctx);
        checksum = FileUtils.getFileChecksum(MessageDigest.getInstance("MD5"), uf.file());
        LOG.warn("The calculated checksum for {} is {}", uf.fileName(), checksum);
        return checksum;
    }

    private void updateAnalyseMetadata(String analyseId, String jvmId, Identifier userId, ParseResult pr) {
        LogMetadata md = pr.getLogMetadata().get();
        analyseRepository.perform(new UpdateJvmInfoOperation(userId, analyseId, jvmId, md.commandLines(),
                new MemoryDetailsImpl(md.pageSize(), md.physicalTotal(), md.physicalFree(),
                        md.swapTotal(), md.swapFree())));
    }

    private void persistObjectAges(String analyseId, String jvmId, ParseResult pr) {
        List<ObjectsAges> oas = new ArrayList<>(1);
        for (AgesState as : pr.getAgesStates()) {
            ObjectsAgesImpl oa = new ObjectsAgesImpl();
            oa.analyseId(analyseId);
            oa.jvmId(jvmId);
            oa.occurred(DateTime.now(DateTimeZone.UTC));
            oa.occupied(as.getOccupied());
            oa.total(as.getTotal());
            oas.add(oa);
        }
        agesStateRepository.add(oas);
    }

    private void uploadLogFile(boolean isSync, String analyseId, String jvmId, String username, File logFile) {
        if (!resourceManager.isDisabled()) {
            Future f = executor.submit(() -> {
                try {
                    LOG.debug("Starting uploading {}", logFile);
                    resourceManager.upload(logFile,
                            "log/" + username + "/" + esc(jvmId) + "/" + esc(analyseId));
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

    private ParseResult parseAndPersist(UploadedFile uf, String jvmId, String checksum, GCAnalyse analyse,
                                        Logger log, Consumer<GCEventImpl> enricher) throws IOException {
        ParserContext pctx = new ParserContext(log, checksum, analyse.jvmGCTypes().get(jvmId),
                analyse.jvmVersions().get(jvmId));
        List<GCEvent> eventsBatch = new ArrayList<>();
        final GCEvent[] firstParsed = new GCEvent[1];
        LazyVal<GCEvent> lastPersistedEvent = LazyVal.ofOpt(() ->
                eventRepository.lastEvent(analyse.id(), jvmId, checksum, firstParsed[0].occurred().minusDays(1)));
        ParseResult pr;
        try (FileInputStream fis = new FileInputStream(uf.file())) {
            pr = logsParser.parse(fis, e -> {
                enricher.accept((GCEventImpl) e);
                if (firstParsed[0] == null) {
                    firstParsed[0] = e;
                }
                if (lastPersistedEvent.get() == null || lastPersistedEvent.get().timestamp() <
                        e.timestamp()) {
                    eventsBatch.add(e);
                    if (eventsBatch.size() == 10) {
                        persist(eventsBatch);
                    }
                } else {
                    log.debug("Skipping event as already persisted: {}, last: {}", e, lastPersistedEvent.get());
                    LOG.debug("Skipping event as already persisted: {}, last: {}", e, lastPersistedEvent.get());
                }
            }, pctx);
        }
        if (eventsBatch.size() != 0) {
            persist(eventsBatch);
        }
        return pr;
    }

    private void persist(List<GCEvent> eventsBatch) {
        try {
            eventRepository.addAsync(eventsBatch);
        } finally {
            eventsBatch.clear();
        }
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

    private Pair<Boolean, GCAnalyse> checkAnalyseBelongsToAccount(RequestContext ctx, String analyseId) {
        Optional<GCAnalyse> analyse = analyseRepository.analyse(analyseId);
        if (!(analyse.isPresent() && analyse.get().accountId().equals(account(ctx).id()))) {
            ctx.write(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM,
                    "An account doesn't contain analyse ID " + analyseId));
            return Pair.of(false, null);
        }
        return Pair.of(true, analyse.get());
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

    protected ThreadLocal<ch.qos.logback.classic.Logger> loggers = new ThreadLocal<ch.qos.logback.classic.Logger>() {
        @Override
        protected ch.qos.logback.classic.Logger initialValue() {
            return ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Thread.currentThread().getName());
        }
    };

    protected ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);
    @Autowired
    protected GCAnalyseRepository analyseRepository;
    @Autowired
    protected GCEventRepository eventRepository;
    @Autowired
    protected VMEventsRepository<ObjectsAges> agesStateRepository;
    @Autowired
    protected LogsParser<ParseResult> logsParser;
    @Autowired
    protected ResourceManager resourceManager;

    protected static class PeriodParams {
        private final String analyseId;
        private final String jvmId;
        private final DateTime from;
        private final DateTime to;
        private final DateTimeZone timeZone;

        public String getAnalyseId() {
            return analyseId;
        }
        public String getJvmId() {
            return jvmId;
        }
        public DateTime getFrom() {
            return from;
        }
        public DateTime getTo() {
            return to;
        }
        public DateTimeZone getTimeZone() {
            return timeZone;
        }

        public PeriodParams(RequestContext ctx) {
            DateTimeZone tz;
            try {
                tz = DateTimeZone.forID(ctx.param("tz", "UTC"));
            } catch (Throwable t) {
                ctx.finish(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM, "Param tz is invalid."));
                throw Exceptions.runtime(t);
            }
            this.timeZone = tz;
            this.analyseId = ctx.param("analyse_id");
            this.jvmId = ctx.param("jvm_id");
            this.from = new DateTime(Long.parseLong(ctx.param("from")), tz);
            this.to = new DateTime(Long.parseLong(ctx.param("to")), tz);
        }
    }
}
