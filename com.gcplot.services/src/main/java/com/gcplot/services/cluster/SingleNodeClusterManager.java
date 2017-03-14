package com.gcplot.services.cluster;

import com.gcplot.cluster.ClusterManager;
import com.gcplot.cluster.Worker;
import com.gcplot.cluster.WorkerTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/14/17
 */
public class SingleNodeClusterManager implements ClusterManager {
    private Worker worker;
    private Map<String, WorkerTask> tasks = new ConcurrentHashMap<>();

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean isMaster() {
        return true;
    }

    @Override
    public Set<Worker> workers() {
        return Collections.singleton(worker);
    }

    @Override
    public List<WorkerTask> retrieveTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void registerTask(WorkerTask task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void completeTask(WorkerTask task) {
        tasks.remove(task.getId());
    }

    @Override
    public boolean isTaskRegistered(String taskId) {
        return tasks.containsKey(taskId);
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }
}
