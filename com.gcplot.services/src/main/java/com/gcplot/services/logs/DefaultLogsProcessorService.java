package com.gcplot.services.logs;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import com.gcplot.Identifier;
import com.gcplot.commons.ConfigProperty;
import com.gcplot.commons.ErrorMessages;
import com.gcplot.commons.LazyVal;
import com.gcplot.commons.exceptions.Exceptions;
import com.gcplot.configuration.ConfigurationManager;
import com.gcplot.logs.*;
import com.gcplot.logs.survivor.AgesState;
import com.gcplot.model.account.Account;
import com.gcplot.model.gc.*;
import com.gcplot.model.gc.vm.VMProperties;
import com.gcplot.model.gc.vm.VMPropertiesDetector;
import com.gcplot.repository.GCAnalyseRepository;
import com.gcplot.repository.GCEventRepository;
import com.gcplot.repository.VMEventsRepository;
import com.gcplot.repository.operations.analyse.AddJvmOperation;
import com.gcplot.repository.operations.analyse.AnalyseOperation;
import com.gcplot.repository.operations.analyse.UpdateCornerEventsOperation;
import com.gcplot.repository.operations.analyse.UpdateJvmInfoOperation;
import com.gcplot.resource.ResourceManager;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.gcplot.commons.CollectionUtils.cloneAndAdd;
import static com.gcplot.commons.CollectionUtils.cloneAndPut;
import static com.gcplot.commons.Utils.esc;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         2/25/17
 */
public class DefaultLogsProcessorService implements LogsProcessorService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultLogsProcessorService.class);
    private static final String ANONYMOUS_ANALYSE_NAME = "Files";
    private static final String LOG_PATTERN = "%d{yyyyMMdd HH:mm:ss.SSS} [[%5p] %c{1} [%t]] %m%n";
    private ThreadLocal<ch.qos.logback.classic.Logger> loggers = ThreadLocal.withInitial(
            () -> ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Thread.currentThread().getName()));
    private ExecutorService uploadExecutor;
    private ResourceManager resourceManager;
    private GCAnalyseRepository analyseRepository;
    private GCEventRepository eventRepository;
    private VMEventsRepository<ObjectsAges> agesStateRepository;
    private LogsParser logsParser;
    private VMPropertiesDetector vmPropertiesDetector;
    private GCAnalyseFactory analyseFactory;
    private ObjectsAgesFactory objectsAgesFactory;
    private ConfigurationManager config;

    public void init() {
        uploadExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 8);
    }

    public void destroy() {
        try {
            uploadExecutor.shutdownNow();
            uploadExecutor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
    }

    @Override
    public LogProcessResult process(LogSource source, Account account, String analyzeId, String jvmId,
                                    boolean sync) {
        try {
            final Identifier userId = account.id();
            GCAnalyse analyze;

            if (Objects.equals(analyzeId, ANONYMOUS_ANALYSE_ID)) {
                Optional<GCAnalyse> oAnalyse = analyseRepository.analyse(userId, analyzeId);
                if (!oAnalyse.isPresent()) {
                    analyze = createFilesAnalyze(analyzeId, userId);
                    analyseRepository.newAnalyse(analyze);
                } else {
                    analyze = oAnalyse.get();
                }
                if (!jvmBelongsAnalyze(jvmId, analyze)) {
                    VMProperties props = vmPropertiesDetector.detect(source);
                    if (props == VMProperties.EMPTY) {
                        return new LogProcessResult(ErrorMessages.buildJson(ErrorMessages.LOG_FILE_UNDETECTABLE));
                    }

                    analyze = addJvmToAnalyze(source, analyzeId, jvmId, userId, analyze, props);
                }
            } else {
                analyze = analyseRepository.analyse(userId, analyzeId).orElse(null);
            }
            return processLog(source, account, jvmId, sync, analyze);
        } catch (Throwable t) {
            throw Exceptions.runtime(t);
        } finally {
            if (source.localFile().isPresent()) {
                uploadLogFile(sync, "gc-log", analyzeId, jvmId, account.username(), source.localFile().get());
            }
            // TODO log file size processed per user
        }
    }

    @Override
    public LogProcessResult process(LogSource source, Account account, GCAnalyse analyse, String jvmId) {
        try {
            return processLog(source, account, jvmId, false, analyse);
        } catch (Throwable t) {
            throw Exceptions.runtime(t);
        } finally {
            if (source.localFile().isPresent()) {
                uploadLogFile(false, "gc-log", analyse.id(), jvmId, account.username(), source.localFile().get());
            }
            // TODO log file size processed per user
        }
    }

    protected LogProcessResult processLog(LogSource source, Account account, String jvmId, boolean sync, GCAnalyse analyze) throws IOException {
        LogProcessResult x = checkAnalyzeCorrect(analyze.id(), jvmId, account.id(), analyze);
        if (x != null) return x;

        final File logFile = java.nio.file.Files.createTempFile("gcplot_log", ".log").toFile();
        Logger log = createLogger(logFile);

        AtomicReference<DateTime> lastEventTime = new AtomicReference<>(null);
        ParseResult pr = parseAndPersist(source, jvmId, analyze, log, e -> {
            if (!e.isOther() && (lastEventTime.get() == null || lastEventTime.get().isBefore(e.occurred()))) {
                lastEventTime.set(e.occurred());
            }
            e.analyseId(analyze.id()).jvmId(jvmId);
        });

        if (pr.isSuccessful()) {
            updateAnalyzeInfo(analyze.id(), jvmId, account.id(), lastEventTime, pr);
            if (pr.getAgesStates().size() > 0) {
                persistObjectAges(analyze.id(), jvmId, pr);
            }
        } else {
            LOG.debug(pr.getException().get().getMessage(), pr.getException().get());
            log.error(pr.getException().get().getMessage(), pr.getException().get());
            return new LogProcessResult(ErrorMessages.buildJson(ErrorMessages.INTERNAL_ERROR));
        }

        truncateFile(logFile, getConfig().readLong(ConfigProperty.PARSE_LOG_MAX_FILE_SIZE));
        uploadLogFile(sync, analyze.id(), jvmId, account.username(), logFile);

        return LogProcessResult.SUCCESS;
    }

    protected void updateAnalyzeInfo(String analyzeId, String jvmId, Identifier userId, AtomicReference<DateTime> lastEventTime, ParseResult pr) {
        List<AnalyseOperation> ops = new ArrayList<>(2);
        if (lastEventTime.get() != null) {
            ops.add(new UpdateCornerEventsOperation(userId, analyzeId, jvmId,
                    pr.getFirstEvent() != null ? pr.getFirstEvent().occurred() : null,
                    lastEventTime.get()));
        }
        if (pr.getLogMetadata().isPresent()) {
            updateAnalyseMetadata(analyzeId, jvmId, userId, pr, ops);
        }
        if (ops.size() > 0) {
            analyseRepository.perform(ops);
        }
    }

    private void persistObjectAges(String analyseId, String jvmId, ParseResult pr) {
        List<ObjectsAges> oas = new ArrayList<>(1);
        for (AgesState as : pr.getAgesStates()) {
            oas.add(objectsAgesFactory.create(analyseId, jvmId, DateTime.now(DateTimeZone.UTC), as.getDesiredSurvivorSize(),
                    as.getOccupied(), as.getTotal(), ""));
        }
        agesStateRepository.add(oas);
    }

    private void updateAnalyseMetadata(String analyseId, String jvmId, Identifier userId, ParseResult pr,
                                       List<AnalyseOperation> ops) {
        LogMetadata md = pr.getLogMetadata().get();
        ops.add(new UpdateJvmInfoOperation(userId, analyseId, jvmId, md.commandLines(),
                new MemoryDetails(md.pageSize(), md.physicalTotal(), md.physicalFree(), md.swapTotal(), md.swapFree())));
    }

    protected GCAnalyse createFilesAnalyze(String analyzeId, Identifier userId) {
        return analyseFactory.create(analyzeId, userId, ANONYMOUS_ANALYSE_NAME, null,
                false, DateTime.now(DateTimeZone.UTC), "");
    }

    protected GCAnalyse addJvmToAnalyze(LogSource source, String analyzeId, String jvmId, Identifier userId, GCAnalyse analyze, VMProperties props) {
        analyseRepository.perform(new AddJvmOperation(userId, analyzeId, jvmId, source.handle().getName(),
                props.getVersion(), props.getGcType(), "", null));
        analyze = analyseFactory.create(analyze, cloneAndAdd(analyze.jvmIds(), jvmId),
                cloneAndPut(analyze.jvmNames(), jvmId, source.handle().getName()),
                cloneAndPut(analyze.jvmVersions(), jvmId, props.getVersion()),
                cloneAndPut(analyze.jvmGCTypes(), jvmId, props.getGcType()));
        return analyze;
    }

    protected boolean jvmBelongsAnalyze(String jvmId, GCAnalyse analyze) {
        return analyze.jvmIds().contains(jvmId);
    }

    protected LogProcessResult checkAnalyzeCorrect(String analyzeId, String jvmId, Identifier userId, GCAnalyse analyze) {
        if (analyze == null) {
            return new LogProcessResult(ErrorMessages.buildJson(ErrorMessages.UNKNOWN_GC_ANALYSE, analyzeId));
        }
        if (!jvmBelongsAnalyze(jvmId, analyze)) {
            return new LogProcessResult(ErrorMessages.buildJson(ErrorMessages.UNKNOWN_JVM_ID, jvmId));
        }
        if (!analyze.accountId().equals(userId)) {
            return new LogProcessResult(ErrorMessages.buildJson(ErrorMessages.INVALID_REQUEST_PARAM, "Analyse doesn't belong to the current account."));
        }
        return null;
    }

    private ParseResult parseAndPersist(LogSource source, String jvmId, GCAnalyse analyse,
                                        Logger log, Consumer<GCEvent> enricher) throws IOException {
        ParserContext ctx = new ParserContext(log, source.checksum(), analyse.jvmGCTypes().get(jvmId),
                analyse.jvmVersions().get(jvmId));
        final GCEvent[] firstParsed = new GCEvent[1];
        final int[] lastMonth = new int[1];
        final DateTime[] lastOccurred = new DateTime[1];
        final boolean forbidOtherGen = config.readBoolean(ConfigProperty.FORBID_OTHER_GENERATION);
        final int batchSize = config.readInt(ConfigProperty.BATCH_PUT_GC_EVENT_SIZE);
        final ThreadLocal<List<GCEvent>> es = ThreadLocal.withInitial(() -> new ArrayList<>(batchSize));
        LazyVal<GCEvent> lastPersistedEvent = LazyVal.ofOpt(() ->
                eventRepository.lastEvent(analyse.id(), jvmId, source.checksum(), firstParsed[0].occurred().minusDays(1)));
        ParseResult pr;
        try (InputStream fis = source.logStream()) {
            pr = logsParser.parse(fis, first -> {
                if (!first.isOther()) {
                    firstParsed[0] = first;
                    lastPersistedEvent.get();
                    return true;
                } else {
                    return false;
                }
            }, e -> {
                List<GCEvent> events = es.get();
                if (e != null) {
                    enricher.accept(e);
                    if (firstParsed[0] == null || lastPersistedEvent.get() == null || lastPersistedEvent.get().timestamp() <
                            e.timestamp() && (!forbidOtherGen || !e.isOther())) {
                        if (events.size() > 0 &&
                                (events.size() % batchSize == 0
                                        || (lastMonth[0] != e.occurred().getMonthOfYear())
                                        || (e.occurred().equals(lastOccurred[0])))) {
                            persist(events);
                        }
                        events.add(e);
                        lastMonth[0] = e.occurred().getMonthOfYear();
                        lastOccurred[0] = e.occurred();
                    }
                } else if (events.size() > 0) {
                    persist(events);
                }
            }, ctx);
        }
        return pr;
    }

    private void persist(List<GCEvent> events) {
        eventRepository.addAsync(events);
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
                            rootFolder + "/" + esc(username) + "/" + esc(analyseId) + "/" + esc(jvmId));
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

    private void truncateFile(File logFile, long maxSize) {
        if (logFile.length() > maxSize) {
            try (FileChannel outChan = new FileOutputStream(logFile, true).getChannel()) {
                outChan.truncate(maxSize);
            } catch (Throwable t) {
                LOG.error(t.getMessage(), t);
            }
        }
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public GCAnalyseRepository getAnalyseRepository() {
        return analyseRepository;
    }

    public void setAnalyseRepository(GCAnalyseRepository analyseRepository) {
        this.analyseRepository = analyseRepository;
    }

    public GCEventRepository getEventRepository() {
        return eventRepository;
    }

    public void setEventRepository(GCEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public VMEventsRepository<ObjectsAges> getAgesStateRepository() {
        return agesStateRepository;
    }

    public void setAgesStateRepository(VMEventsRepository<ObjectsAges> agesStateRepository) {
        this.agesStateRepository = agesStateRepository;
    }

    public LogsParser getLogsParser() {
        return logsParser;
    }

    public void setLogsParser(LogsParser logsParser) {
        this.logsParser = logsParser;
    }

    public VMPropertiesDetector getVmPropertiesDetector() {
        return vmPropertiesDetector;
    }

    public void setVmPropertiesDetector(VMPropertiesDetector vmPropertiesDetector) {
        this.vmPropertiesDetector = vmPropertiesDetector;
    }

    public GCAnalyseFactory getAnalyseFactory() {
        return analyseFactory;
    }

    public void setAnalyseFactory(GCAnalyseFactory analyseFactory) {
        this.analyseFactory = analyseFactory;
    }

    public ObjectsAgesFactory getObjectsAgesFactory() {
        return objectsAgesFactory;
    }

    public void setObjectsAgesFactory(ObjectsAgesFactory objectsAgesFactory) {
        this.objectsAgesFactory = objectsAgesFactory;
    }

    public ConfigurationManager getConfig() {
        return config;
    }

    public void setConfig(ConfigurationManager config) {
        this.config = config;
    }
}
