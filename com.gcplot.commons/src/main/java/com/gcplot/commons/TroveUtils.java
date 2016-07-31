package com.gcplot.commons;

import gnu.trove.impl.unmodifiable.TUnmodifiableIntList;
import gnu.trove.impl.unmodifiable.TUnmodifiableIntSet;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/31/16
 */
public abstract class TroveUtils {
    private static final TIntList EMPTY_INT_LIST = new TUnmodifiableIntList(new TIntArrayList());
    private static final TIntSet EMPTY_INT_SET = new TUnmodifiableIntSet(new TIntHashSet());

    public static TIntList emptyIntList() {
        return EMPTY_INT_LIST;
    }

    public static TIntSet emptyIntSet() {
        return EMPTY_INT_SET;
    }

}
