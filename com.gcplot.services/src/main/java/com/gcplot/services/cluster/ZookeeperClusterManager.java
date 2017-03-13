package com.gcplot.services.cluster;

import com.gcplot.cluster.ClusterManager;
import com.gcplot.cluster.WorkerTask;
import com.gcplot.cluster.Worker;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/9/17
 */
public class ZookeeperClusterManager implements ClusterManager {
    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperClusterManager.class);
    private static final String WORKERS_PATH = "/workers";
    private static final String TASKS_PATH = "/tasks";
    private static final String ENQUEUED_PATH = "/enqueued";
    private static final String ELECTION_PATH = "/election";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ZookeeperConnector connector;
    private Worker currentWorker;
    private boolean syncElection;
    private volatile boolean isMaster = false;

    public void init() {
        if (syncElection) {
            leaderElection();
        } else {
            executor.execute(this::leaderElection);
        }
    }

    public void destroy() {
        executor.shutdownNow();
    }

    @Override
    public boolean isConnected() {
        return connector.getClient().getState().isConnected();
    }

    @Override
    public boolean isMaster() {
        return isMaster;
    }

    @Override
    public List<Worker> workers() {
        return null;
    }

    @Override
    public List<WorkerTask> retrieveTasks() {
        return null;
    }

    @Override
    public void proceedTask(WorkerTask task) {

    }

    @Override
    public void completeTask(WorkerTask task) {

    }

    @Override
    public boolean isTaskEnqueued(WorkerTask task) {
        return false;
    }

    private void leaderElection() {
        leaderElection(true);
    }

    private void register() {
        try {
            String path = WORKERS_PATH + "/" + currentWorker.getHostname();
            Stat stat = zk().exists(path, false);
            if (stat == null) {
                connector.create(path, )
            }
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
    }

    private void leaderElection(boolean performNewVoting) {
        try {
            debug("starting new leader election.");
            Stat stat = zk().exists(ELECTION_PATH, false);
            if (stat == null) {
                debug("Election path didn't exist, creating new one.");
                String r = connector.create(ELECTION_PATH);
                debug("Election path created: {}", r);
            }

            if (performNewVoting) {
                String childPath = connector.create(ELECTION_PATH + "/n_", CreateMode.EPHEMERAL_SEQUENTIAL);
                debug("leader voting node created: {}", childPath);
            }

            List<String> children = connector.getClient().getChildren(ELECTION_PATH, false);

            String tmp = children.get(0);
            for (String s : children) {
                if (tmp.compareTo(s) > 0)
                    tmp = s;
            }

            String leader = ELECTION_PATH + "/" + tmp;
            Stat s = connector.getClient().exists(leader, event -> {
                if (event.getType() == Watcher.Event.EventType.NodeDeleted) {
                    executor.execute(() -> leaderElection(false));
                }
            });
            if (s == null) {
                debug("for some reason winning node lost, retrying.");
                executor.execute(this::leaderElection);
            } else {
                debug("new leader path: {}, id: {}", leader, s.getEphemeralOwner());
                isMaster = connector.getClient().getSessionId() == s.getEphemeralOwner();
            }
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
    }

    private void debug(String msg, Object... params) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ZCM (" + connector.getClient().getSessionId() + "): " + msg, params);
        }
    }

    private ZooKeeper zk() {
        return connector.getClient();
    }

    public void setConnector(ZookeeperConnector connector) {
        this.connector = connector;
    }

    public void setSyncElection(boolean syncElection) {
        this.syncElection = syncElection;
    }

    public void setCurrentWorker(Worker currentWorker) {
        this.currentWorker = currentWorker;
    }
}
