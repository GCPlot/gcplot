package com.gcplot.services.cluster;

import com.gcplot.cluster.ClusterManager;
import com.gcplot.cluster.WorkerTask;
import com.gcplot.cluster.Worker;
import com.gcplot.utils.Exceptions;
import com.gcplot.commons.serialization.ProtostuffSerializer;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/9/17
 */
public class ZookeeperClusterManager implements ClusterManager {
    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperClusterManager.class);
    private static final String WORKERS_PATH = "/workers";
    private static final String TASKS_PATH = "/tasks";
    private static final String TASK_QUEUE_PATH = "/task_queue";
    private static final String ELECTION_PATH = "/election";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ZookeeperConnector connector;
    private Worker currentWorker;
    private boolean syncElection;
    private volatile boolean isMaster = false;

    public void init() {
        register();
        if (syncElection) {
            leaderElection();
        } else {
            executor.execute(this::leaderElection);
        }
    }

    public void destroy() {
        executor.shutdownNow();
        try {
            connector.getClient().close();
        } catch (InterruptedException ignored) {}
    }

    @Override
    public boolean isConnected() {
        return connector.getClient().getState().isConnected();
    }

    @Override
    public boolean isMaster() {
        return isMaster && isConnected();
    }

    @Override
    public Set<Worker> workers() {
        try {
            return connector.children(WORKERS_PATH).stream()
                    .map(p -> WORKERS_PATH + "/" + p)
                    .map(p -> Pair.of(p, connector.exists(p)))
                    .filter(p -> Objects.nonNull(p.getRight()))
                    .map(p -> connector.data(p.getLeft(), p.getRight()))
                    .filter(Objects::nonNull)
                    .map(b -> ProtostuffSerializer.deserialize(Worker.class, b))
                    .collect(Collectors.toSet());
        } catch (Throwable t) {
            throw Exceptions.runtime(t);
        }
    }

    @Override
    public List<WorkerTask> retrieveTasks() {
        String base = TASKS_PATH + "/" + currentWorker.getHostname();
        return connector.children(base).stream()
                .map(p -> base + "/" + p)
                .map(p -> Pair.of(p, connector.exists(p)))
                .filter(p -> Objects.nonNull(p.getRight()))
                .map(p -> connector.data(p.getLeft(), p.getRight()))
                .filter(Objects::nonNull)
                .map(b -> ProtostuffSerializer.deserialize(WorkerTask.class, b))
                .collect(Collectors.toList());
    }

    @Override
    public void registerTask(WorkerTask task) {
        byte[] data = ProtostuffSerializer.serialize(task);
        connector.create(TASK_QUEUE_PATH + "/" + task.getId(), data, CreateMode.EPHEMERAL);
        connector.create(TASKS_PATH + "/" + task.getAssigned().getHostname() + "/" + task.getId(),
                data, CreateMode.EPHEMERAL);
    }

    @Override
    public void completeTask(WorkerTask task) {
        connector.delete(TASK_QUEUE_PATH + "/" + task.getId());
        connector.delete(TASKS_PATH + "/" + task.getAssigned().getHostname() + "/" + task.getId());
    }

    @Override
    public boolean isTaskRegistered(String taskId) {
        return connector.exists(TASK_QUEUE_PATH + "/" + taskId) != null;
    }

    private void leaderElection() {
        leaderElection(true);
    }

    private void register() {
        if (connector.exists(TASKS_PATH) == null) {
            connector.create(TASKS_PATH, CreateMode.PERSISTENT);
        }
        if (connector.exists(WORKERS_PATH) == null) {
            connector.create(WORKERS_PATH, CreateMode.PERSISTENT);
        }
        if (connector.exists(TASK_QUEUE_PATH) == null) {
            connector.create(TASK_QUEUE_PATH, CreateMode.PERSISTENT);
        }
        String path = TASKS_PATH + "/" + currentWorker.getHostname();
        if (connector.exists(path) == null) {
            connector.create(path, CreateMode.PERSISTENT);
        }
        path = WORKERS_PATH + "/" + currentWorker.getHostname();
        if (connector.exists(path) == null) {
            connector.create(path, ProtostuffSerializer.serialize(currentWorker), CreateMode.EPHEMERAL);
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

    public Worker getCurrentWorker() {
        return currentWorker;
    }

    public void setCurrentWorker(Worker currentWorker) {
        this.currentWorker = currentWorker;
    }
}
