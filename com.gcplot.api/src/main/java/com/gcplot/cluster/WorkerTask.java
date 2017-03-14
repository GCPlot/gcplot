package com.gcplot.cluster;

import java.util.Arrays;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/9/17
 */
public class WorkerTask {
    private final String id;
    private final Worker assigned;
    private final Object task;

    public WorkerTask(String id, Worker assigned, Object task) {
        this.id = id;
        this.assigned = assigned;
        this.task = task;
    }

    public String getId() {
        return id;
    }

    public Worker getAssigned() {
        return assigned;
    }

    public Object getTask() {
        return task;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorkerTask that = (WorkerTask) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (assigned != null ? !assigned.equals(that.assigned) : that.assigned != null) return false;
        return task != null ? task.equals(that.task) : that.task == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (assigned != null ? assigned.hashCode() : 0);
        result = 31 * result + (task != null ? task.hashCode() : 0);
        return result;
    }
}
