package com.gcplot.processing;

import com.gcplot.cluster.ClusterManager;
import com.gcplot.cluster.WorkerTask;
import com.gcplot.fs.LogsStorageProvider;
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
    private GCAnalyseRepository analyseRepository;
    private LogsStorageProvider logsStorageProvider;

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
       List<WorkerTask> tasks = clusterManager.retrieveTasks();
       for (WorkerTask task : tasks) {

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
}
