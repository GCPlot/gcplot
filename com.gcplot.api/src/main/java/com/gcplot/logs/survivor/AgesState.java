package com.gcplot.logs.survivor;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/30/16
 */
public class AgesState {
    private final long desiredSurvivorSize;
    public long getDesiredSurvivorSize() {
        return desiredSurvivorSize;
    }

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

    public AgesState(long desiredSurvivorSize, List<Long> occupied, List<Long> total) {
        Preconditions.checkState(occupied.size() == total.size(), "Unknown situation where occupied.lenght != total.length");
        this.desiredSurvivorSize = desiredSurvivorSize;
        this.ages = occupied.size();
        this.occupied = Collections.unmodifiableList(occupied);
        this.total = Collections.unmodifiableList(total);
    }

    public static AgesState single(long desiredSurvivorSize, Pair<Long, Long> sizes) {
        return new AgesState(desiredSurvivorSize, Collections.singletonList(sizes.getLeft()),
                Collections.singletonList(sizes.getRight()));
    }

    public static final AgesState NONE = new AgesState(0L, Collections.emptyList(), Collections.emptyList());

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AgesState{");
        sb.append("desiredSurvivorSize=").append(desiredSurvivorSize);
        sb.append(", ages=").append(ages);
        sb.append(", occupied=").append(occupied);
        sb.append(", total=").append(total);
        sb.append('}');
        return sb.toString();
    }
}
