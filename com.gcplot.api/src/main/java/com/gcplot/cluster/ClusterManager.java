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

    List<LogProcessTask> retrieveTasks();

    void completeTask(LogProcessTask task);

}
