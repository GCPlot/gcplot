package com.gcplot.cluster;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/9/17
 */
public interface ClusterManager {

    boolean isConnected();

    boolean isMaster();

    Set<Worker> workers();

    List<WorkerTask> retrieveTasks();

    void registerTask(WorkerTask task);

    void completeTask(WorkerTask task);

    boolean isTaskRegistered(String taskId);

}
