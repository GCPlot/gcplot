package com.gcplot.processing;

import com.gcplot.cluster.ClusterManager;
import com.gcplot.cluster.Worker;
import com.gcplot.cluster.WorkerTask;
import com.gcplot.fs.LogsStorage;
import com.gcplot.fs.LogsStorageProvider;
import com.gcplot.logs.LogHandle;
import com.gcplot.model.gc.GCAnalyse;
import com.gcplot.model.gc.SourceType;
import com.gcplot.repository.GCAnalyseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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

    public void destroy() throws Exception {
        timer.shutdown();
        timer.awaitTermination(WAIT_SHUTDOWN_MINUTES, TimeUnit.MINUTES);
    }

    public void scan() {
        LOG.debug("WorkerTaskDistributor: Starting job.");
        if (!clusterManager.isConnected()) {
            LOG.info("WorkerTaskDistributor: Disconnected from cluster. Skipping job.");
            return;
        }
        if (!clusterManager.isMaster()) {
            LOG.debug("WorkerTaskDistributor: Not a master. Quiting.");
        }
        List<Worker> workers = new ArrayList<>(clusterManager.workers());
        SecureRandom r = new SecureRandom();
        try {
            Iterator<GCAnalyse> i = analyseRepository.analyses(true).iterator();
            while (i.hasNext()) {
                GCAnalyse analyze = i.next();
                LOG.debug("WorkerTaskDistributor: Checking analyze {} with source {}", analyze.id(), analyze.sourceType());
                if (analyze.sourceType() != SourceType.NONE) {
                    try {
                        LogsStorage logsStorage =
                                logsStorageProvider.get(analyze.sourceType(), analyze.sourceConfigProps());
                        if (logsStorage != null) {
                            Iterator<LogHandle> handles = logsStorage.listAll();
                            while (handles.hasNext()) {
                                LogHandle logHandle = handles.next();
                                if (clusterManager.isMaster() && !clusterManager.isTaskRegistered(logHandle.hash())) {
                                    WorkerTask task = new WorkerTask(logHandle.hash(),
                                            workers.get(r.nextInt(workers.size())), logHandle);
                                    LOG.debug("Assigning task {} to worker {}", task.getId(), task.getAssigned().getHostname());
                                    clusterManager.registerTask(task);
                                }
                            }
                        } else {
                            LOG.debug("WorkerTaskDistributor: {}: no storage for {} [{}]", analyze.id(),
                                    analyze.sourceType(), analyze.sourceConfig());
                        }
                    } catch (Throwable t) {
                        LOG.error(analyze.id() + ": " + t.getMessage(), t);
                    }
                }
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
