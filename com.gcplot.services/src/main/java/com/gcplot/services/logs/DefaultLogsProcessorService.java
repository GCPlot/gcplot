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
import com.gcplot.model.account.Account;
import com.gcplot.model.gc.GCAnalyse;
import com.gcplot.model.gc.GCAnalyseFactory;
import com.gcplot.model.gc.GCEvent;
import com.gcplot.model.gc.Generation;
import com.gcplot.model.gc.vm.VMProperties;
import com.gcplot.model.gc.vm.VMPropertiesDetector;
import com.gcplot.repository.GCAnalyseRepository;
import com.gcplot.repository.GCEventRepository;
import com.gcplot.repository.operations.analyse.AddJvmOperation;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.gcplot.commons.CollectionUtils.cloneAndAdd;
import static com.gcplot.commons.CollectionUtils.cloneAndPut;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         2/25/17
 */
public class DefaultLogsProcessorService implements LogsProcessorService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultLogsProcessorService.class);
    private static final EnumSet<Generation> OTHER_GENERATION = EnumSet.of(Generation.OTHER);
    public static final String ANONYMOUS_ANALYSE_ID = "7acada7b-e109-4d11-ac01-b3521d9d58c3";
    private static final String ANONYMOUS_ANALYSE_NAME = "Files";
    private static final String LOG_PATTERN = "%d{yyyyMMdd HH:mm:ss.SSS} [[%5p] %c{1} [%t]] %m%n";
    private ThreadLocal<ch.qos.logback.classic.Logger> loggers = ThreadLocal.withInitial(
            () -> ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Thread.currentThread().getName()));
    private ExecutorService uploadExecutor;
    private GCAnalyseRepository analyseRepository;
    private GCEventRepository eventRepository;
    private LogsParser logsParser;
    private VMPropertiesDetector vmPropertiesDetector;
    private GCAnalyseFactory analyseFactory;
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
            LogProcessResult x = checkAnalyzeCorrect(analyzeId, jvmId, userId, analyze);
            if (x != null) return x;

            String checksum = source.checksum().orElse(null);
            if (checksum == null) {
                return new LogProcessResult(ErrorMessages.buildJson(ErrorMessages.INTERNAL_ERROR, "Unable to detect checksum."));
            }

            final File logFile = java.nio.file.Files.createTempFile("gcplot_log", ".log").toFile();
            Logger log = createLogger(logFile);



            return null;
        } catch (Throwable t) {
            throw Exceptions.runtime(t);
        } finally {

        }
    }

    protected GCAnalyse createFilesAnalyze(String analyzeId, Identifier userId) {
        return analyseFactory.create(analyzeId, userId, ANONYMOUS_ANALYSE_NAME, null,
                false, DateTime.now(DateTimeZone.UTC), "");
    }

    protected GCAnalyse addJvmToAnalyze(LogSource source, String analyzeId, String jvmId, Identifier userId, GCAnalyse analyze, VMProperties props) {
        analyseRepository.perform(new AddJvmOperation(userId, analyzeId, jvmId, source.name(),
                props.getVersion(), props.getGcType(), "", null));
        analyze = analyseFactory.create(analyze, cloneAndAdd(analyze.jvmIds(), jvmId),
                cloneAndPut(analyze.jvmNames(), jvmId, source.name()),
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

    private ParseResult parseAndPersist(boolean isSync, LogSource source, String jvmId, String checksum, GCAnalyse analyse,
                                        Logger log, Consumer<GCEvent> enricher) throws IOException {
        ParserContext ctx = new ParserContext(log, checksum, analyse.jvmGCTypes().get(jvmId),
                analyse.jvmVersions().get(jvmId));
        final GCEvent[] firstParsed = new GCEvent[1];
        LazyVal<GCEvent> lastPersistedEvent = LazyVal.ofOpt(() ->
                eventRepository.lastEvent(analyse.id(), jvmId, checksum, firstParsed[0].occurred().minusDays(1)));
        ParseResult pr;
        try (InputStream fis = source.logStream()) {
            pr = logsParser.parse(fis, first -> {
                        firstParsed[0] = first;
                        lastPersistedEvent.get();
                    }, enricher,
                    e -> {
                        enricher.accept(e);
                        if (lastPersistedEvent.get() == null || lastPersistedEvent.get().timestamp() <
                                e.timestamp()) {
                            persist(isSync, e);
                        } else {
                            log.debug("Skipping event as already persisted: {}, last: {}", e, lastPersistedEvent.get());
                            LOG.debug("Skipping event as already persisted: {}, last: {}", e, lastPersistedEvent.get());
                        }
                    }, ctx);
        }
        return pr;
    }

    private void persist(boolean isSync, GCEvent event) {
        if (!config.readBoolean(ConfigProperty.FORBID_OTHER_GENERATION) ||
                !event.generations().equals(OTHER_GENERATION)) {
            if (isSync) {
                eventRepository.add(event);
            } else {
                eventRepository.addAsync(event);
            }
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

    public ConfigurationManager getConfig() {
        return config;
    }

    public void setConfig(ConfigurationManager config) {
        this.config = config;
    }
}
