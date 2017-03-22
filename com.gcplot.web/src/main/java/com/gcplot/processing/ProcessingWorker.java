package com.gcplot.processing;

import com.gcplot.Identifier;
import com.gcplot.cluster.ClusterManager;
import com.gcplot.cluster.WorkerTask;
import com.gcplot.fs.LogsStorage;
import com.gcplot.fs.LogsStorageProvider;
import com.gcplot.logs.LogHandle;
import com.gcplot.logs.LogSource;
import com.gcplot.logs.LogsProcessorService;
import com.gcplot.model.gc.GCAnalyse;
import com.gcplot.repository.AccountRepository;
import com.gcplot.repository.GCAnalyseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TODO in future it should be event listener, not timer
 *
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/19/17
 */
public class ProcessingWorker {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessingWorker.class);
    private static final long WAIT_SHUTDOWN_MINUTES = 10;
    private long intervalMs;
    private ScheduledExecutorService timer;
    private ClusterManager clusterManager;
    private AccountRepository accountRepository;
    private GCAnalyseRepository analyseRepository;
    private LogsStorageProvider logsStorageProvider;
    private LogsProcessorService logsProcessor;

    public void init() {
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(this::processOfflineLogs, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
    }

    public void destroy() throws InterruptedException {
        timer.shutdown();
        timer.awaitTermination(WAIT_SHUTDOWN_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * Entry point for processing logs from S3, GCS, etc.
     */
    public void processOfflineLogs() {
        LOG.debug("ProcessingWorker: Starting offline logs processing.");
        try {
            List<WorkerTask> tasks = clusterManager.retrieveTasks();
            for (WorkerTask task : tasks) {
                try {
                    LogHandle logHandle = task.getTask();
                    Identifier accountId = accountRepository.map(logHandle.getUsername()).orElse(null);
                    if (accountId != null) {
                        GCAnalyse analyze = analyseRepository.analyse(accountId, logHandle.getAnalyzeId()).orElse(null);
                        if (analyze != null) {
                            if (analyze.jvmIds().contains(logHandle.getJvmId())) {
                                LogsStorage logsStorage = logsStorageProvider.get(analyze.sourceType(), analyze.sourceConfigProps());
                                if (logsStorage != null) {
                                    LogSource logSource = logsStorage.get(logHandle);

                                    logsProcessor.process(logSource, accountRepository.account(accountId).orElse(null),
                                            analyze, logHandle.getJvmId());
                                    logsStorage.delete(logHandle);
                                    clusterManager.completeTask(task);
                                } else {
                                    LOG.debug("ProcessingWorker: {}: no storage for {} [{}]", analyze.id(), analyze.sourceType(), analyze.sourceConfig());
                                }
                            } else {
                                LOG.debug("ProcessingWorker: JVM {} was not found in analyze {} for account [{}:{}]",
                                        logHandle.getJvmId(), logHandle.getAnalyzeId(), accountId, logHandle.getUsername());
                            }
                        } else {
                            LOG.debug("ProcessingWorker: analyze {} not found for account [{}:{}]", logHandle.getAnalyzeId(),
                                    accountId, logHandle.getUsername());
                        }
                    } else {
                        LOG.debug("ProcessingWorker: account {} not found.", logHandle.getUsername());
                    }
                } catch (Throwable t) {
                    LOG.error(t.getMessage(), t);
                }
            }
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
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

    public void setLogsProcessor(LogsProcessorService logsProcessor) {
        this.logsProcessor = logsProcessor;
    }
}
