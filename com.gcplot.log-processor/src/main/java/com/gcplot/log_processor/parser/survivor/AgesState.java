package com.gcplot.log_processor.parser.survivor;

import com.google.common.base.Preconditions;
import gnu.trove.impl.unmodifiable.TUnmodifiableIntList;
import gnu.trove.list.TIntList;

import static com.gcplot.commons.TroveUtils.emptyIntList;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/30/16
 */
public class AgesState {

    private final int ages;
    public int getAges() {
        return ages;
    }

    private final TIntList occupied;
    public TIntList getOccupied() {
        return occupied;
    }

    private final TIntList total;
    public TIntList getTotal() {
        return total;
    }

    public AgesState(TIntList occupied, TIntList total) {
        Preconditions.checkState(occupied.size() == total.size(), "Unknown situation where occupied.lenght != total.length");
        this.ages = occupied.size();
        this.occupied = new TUnmodifiableIntList(occupied);
        this.total = new TUnmodifiableIntList(total);
    }

    public static final AgesState NONE = new AgesState(emptyIntList(), emptyIntList());

    @Override
    public String toString() {
        return "AgesState{" +
                "ages=" + ages +
                ", occupied=" + occupied +
                ", total=" + total +
                '}';
    }
}
