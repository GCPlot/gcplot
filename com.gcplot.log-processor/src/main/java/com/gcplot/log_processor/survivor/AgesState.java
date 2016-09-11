package com.gcplot.log_processor.survivor;

import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/30/16
 */
public class AgesState {

    private final int ages;
    public int getAges() {
        return ages;
    }

    private final List<Long> occupied;
    public List<Long> getOccupied() {
        return occupied;
    }

    private final List<Long> total;
    public List<Long> getTotal() {
        return total;
    }

    public AgesState(List<Long> occupied, List<Long> total) {
        Preconditions.checkState(occupied.size() == total.size(), "Unknown situation where occupied.lenght != total.length");
        this.ages = occupied.size();
        this.occupied = Collections.unmodifiableList(occupied);
        this.total = Collections.unmodifiableList(total);
    }

    public static final AgesState NONE = new AgesState(Collections.emptyList(), Collections.emptyList());

    @Override
    public String toString() {
        return "AgesState{" +
                "ages=" + ages +
                ", occupied=" + occupied +
                ", total=" + total +
                '}';
    }
}
