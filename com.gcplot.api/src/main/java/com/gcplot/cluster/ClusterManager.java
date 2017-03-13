package com.gcplot.cluster;

import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/9/17
 */
public interface ClusterManager {

    boolean isConnected();

    boolean isMaster();

    List<Worker> workers();

    List<WorkerTask> retrieveTasks();

    void proceedTask(WorkerTask task);

    void completeTask(WorkerTask task);

    boolean isTaskEnqueued(WorkerTask task);

}
