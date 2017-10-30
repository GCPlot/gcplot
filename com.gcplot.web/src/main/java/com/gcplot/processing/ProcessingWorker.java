package com.gcplot.processing;

import com.codahale.metrics.MetricRegistry;
import com.gcplot.Identifier;
import com.gcplot.cluster.ClusterManager;
import com.gcplot.cluster.WorkerTask;
import com.gcplot.commons.Metrics;
import com.gcplot.fs.LogsStorage;
import com.gcplot.fs.LogsStorageProvider;
import com.gcplot.logs.LogHandle;
import com.gcplot.logs.LogSource;
import com.gcplot.logs.LogsProcessorService;
import com.gcplot.model.gc.analysis.GCAnalyse;
import com.gcplot.repository.AccountRepository;
import com.gcplot.repository.GCAnalyseRepository;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * TODO in future it should be event listener, not timer
 *
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/19/17
 */
public class ProcessingWorker {
    private static final String CHECKED_TASKS = Metrics.name(ProcessingWorker.class, "tasks");
    private static final String PROCESSED_METRIC = Metrics.name(ProcessingWorker.class, "processed");
    private static final String PROCESSED_ERROR_METRIC = Metrics.name(ProcessingWorker.class, "processed", "errors");
    private static final Logger LOG = LoggerFactory.getLogger(ProcessingWorker.class);
    private static final long WAIT_SHUTDOWN_MINUTES = 10;
    private long intervalMs;
    private ScheduledExecutorService timer;
    private ExecutorService executorService;
    private ClusterManager clusterManager;
    private AccountRepository accountRepository;
    private GCAnalyseRepository analyseRepository;
    private LogsStorageProvider logsStorageProvider;
    private LogsProcessorService logsProcessor;
    private MetricRegistry metrics;
    private Set<String> inProgress = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void init() {
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(this::processOfflineLogs, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2,
                new ThreadFactoryBuilder().setNameFormat("processing-worker-%d").build());
    }

    public void destroy() {
        try {
            timer.shutdown();
            timer.awaitTermination(1, TimeUnit.MINUTES);
        } catch (Throwable ignored) {}
        try {
            executorService.shutdown();
            executorService.awaitTermination(WAIT_SHUTDOWN_MINUTES, TimeUnit.MINUTES);
        } catch (Throwable ignored) {}
    }

    /**
     * Entry point for processing logs from S3, GCS, etc.
     */
    public void processOfflineLogs() {
        LOG.debug("Starting offline logs processing.");
        try {
            List<WorkerTask> tasks = clusterManager.retrieveTasks();
            for (WorkerTask task : tasks) {
                try {
                    LogHandle logHandle = task.getTask();
                    if (logHandle.equals(LogHandle.INVALID_LOG)) {
                        complete(task);
                        continue;
                    }
                    if (inProgress.contains(logHandle.hash())) {
                        continue;
                    }
                    metrics.meter(CHECKED_TASKS).mark();
                    LOG.debug("Processing handle: {}", logHandle);
                    Identifier accountId = Identifier.fromStr(logHandle.getAccountId());
                    GCAnalyse analyze = analyseRepository.analyse(accountId, logHandle.getAnalyzeId()).orElse(null);
                    if (analyze != null) {
                        if (analyze.jvmIds().contains(logHandle.getJvmId())) {
                            LogsStorage logsStorage = logsStorageProvider.get(analyze.sourceType(), analyze.sourceConfigProps());
                            if (logsStorage != null) {
                                LogSource logSource = logsStorage.get(logHandle);

                                LOG.debug("Running {}, process {}", analyze.id(), logHandle);
                                inProgress.add(logHandle.hash());
                                executorService.submit(() -> {
                                    LOG.debug("Processing {}, process {}", analyze.id(), logHandle);
                                    try {
                                        logsProcessor.process(logSource, accountRepository.account(accountId).orElse(null),
                                                analyze, logHandle.getJvmId());
                                        logsStorage.delete(logHandle);
                                        complete(task);
                                        metrics.meter(PROCESSED_METRIC).mark();
                                    } catch (Throwable t) {
                                        metrics.meter(PROCESSED_ERROR_METRIC).mark();
                                        LOG.error("ProcessingWorker: " + t.getMessage(), t);
                                    } finally {
                                        inProgress.remove(logHandle.hash());
                                    }
                                });

                            } else {
                                complete(task);
                                LOG.debug("{}: no storage for {} [{}]", analyze.id(), analyze.sourceType(), analyze.sourceConfig());
                            }
                        } else {
                            complete(task);
                            LOG.debug("JVM {} was not found in analyze {} for account [{}]",
                                    logHandle.getJvmId(), logHandle.getAnalyzeId(), accountId);
                        }
                    } else {
                        complete(task);
                        LOG.debug("Analyze {} not found for account [{}]", logHandle.getAnalyzeId(), accountId);
                    }
                } catch (Throwable t) {
                    complete(task);
                    metrics.meter(PROCESSED_ERROR_METRIC).mark();
                    LOG.error(t.getMessage(), t);
                }
            }
        } catch (Throwable t) {
            metrics.meter(PROCESSED_ERROR_METRIC).mark();
            LOG.error(t.getMessage(), t);
        }
    }

    private void complete(WorkerTask task) {
        try {
            clusterManager.completeTask(task);
        } catch (Throwable t) {
            LOG.error("complete", t);
        }
    }

    public void setClusterManager(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    public void setAnalyseRepository(GCAnalyseRepository analyseRepository) {
        this.analyseRepository = analyseRepository;
    }

    public void setIntervalMs(long intervalMs) {
        this.intervalMs = intervalMs;
    }

    public void setLogsStorageProvider(LogsStorageProvider logsStorageProvider) {
        this.logsStorageProvider = logsStorageProvider;
    }

    public void setAccountRepository(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void setMetrics(MetricRegistry metrics) {
        this.metrics = metrics;
    }

    public void setLogsProcessor(LogsProcessorService logsProcessor) {
        this.logsProcessor = logsProcessor;
    }
}
