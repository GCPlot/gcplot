package com.gcplot.log_processor.survivor;

import com.google.common.base.Preconditions;
import gnu.trove.impl.unmodifiable.TUnmodifiableLongList;
import gnu.trove.list.TLongList;

import static com.gcplot.commons.TroveUtils.emptyLongList;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/30/16
 */
public class AgesState {

    private final int ages;
    public int getAges() {
        return ages;
    }

    private final TLongList occupied;
    public TLongList getOccupied() {
        return occupied;
    }

    private final TLongList total;
    public TLongList getTotal() {
        return total;
    }

    public AgesState(TLongList occupied, TLongList total) {
        Preconditions.checkState(occupied.size() == total.size(), "Unknown situation where occupied.lenght != total.length");
        this.ages = occupied.size();
        this.occupied = new TUnmodifiableLongList(occupied);
        this.total = new TUnmodifiableLongList(total);
    }

    public static final AgesState NONE = new AgesState(emptyLongList(), emptyLongList());

    @Override
    public String toString() {
        return "AgesState{" +
                "ages=" + ages +
                ", occupied=" + occupied +
                ", total=" + total +
                '}';
    }
}
