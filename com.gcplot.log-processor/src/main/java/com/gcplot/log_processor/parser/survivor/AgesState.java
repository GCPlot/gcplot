package com.gcplot.log_processor.parser.survivor;

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

    private final TIntList sizes;
    public TIntList getSize() {
        return sizes;
    }

    private final TIntList totals;
    public TIntList getTotal() {
        return totals;
    }

    public AgesState(int ages, TIntList sizes, TIntList totals) {
        this.ages = ages;
        this.sizes = new TUnmodifiableIntList(sizes);
        this.totals = new TUnmodifiableIntList(totals);
    }

    public static final AgesState NONE = new AgesState(0, emptyIntList(), emptyIntList());
}
