package com.gcplot.cluster;

import java.util.Arrays;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/9/17
 */
public class WorkerTask {
    private final String id;
    private final Worker assigned;
    private final byte[] data;

    public WorkerTask(String id, Worker assigned, byte[] data) {
        this.id = id;
        this.assigned = assigned;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public Worker getAssigned() {
        return assigned;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorkerTask that = (WorkerTask) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (assigned != null ? !assigned.equals(that.assigned) : that.assigned != null) return false;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (assigned != null ? assigned.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
}
