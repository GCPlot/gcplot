package com.gcplot.processing;

import com.gcplot.cluster.ClusterManager;
import com.gcplot.fs.LogsStorageProvider;
import com.gcplot.model.gc.GCAnalyse;
import com.gcplot.repository.GCAnalyseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Master which is responsible for task distribution to workers.
 *
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/22/17
 */
public class WorkerTaskDistributor {
    private static final Logger LOG = LoggerFactory.getLogger(WorkerTaskDistributor.class);
    private static final long WAIT_SHUTDOWN_MINUTES = 3;
    private long intervalMs;
    private ScheduledExecutorService timer;
    private ClusterManager clusterManager;
    private GCAnalyseRepository analyseRepository;
    private LogsStorageProvider logsStorageProvider;

    public void init() {
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(this::scan, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
    }

    public void shutdown() throws Exception {
        timer.shutdown();
        timer.awaitTermination(WAIT_SHUTDOWN_MINUTES, TimeUnit.MINUTES);
    }

    public void scan() {
        try {
            Iterator<GCAnalyse> i = analyseRepository.analyses(true).iterator();
            while (i.hasNext()) {
                GCAnalyse analyse = i.next();

            }
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
    }

    public void setIntervalMs(long intervalMs) {
        this.intervalMs = intervalMs;
    }

    public void setClusterManager(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    public void setAnalyseRepository(GCAnalyseRepository analyseRepository) {
        this.analyseRepository = analyseRepository;
    }

    public void setLogsStorageProvider(LogsStorageProvider logsStorageProvider) {
        this.logsStorageProvider = logsStorageProvider;
    }
}
