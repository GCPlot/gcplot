package com.gcplot.model.gc;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         5/10/17
 */
public abstract class GCEvents {

    public static boolean lightEquals(GCEvent a, GCEvent b) {
        if (a == b) return true;
        if (a == null) return false;
        if (b == null || a.getClass() != b.getClass()) return false;

        if (Double.compare(b.timestamp(), a.timestamp()) != 0) return false;
        if (a.properties() != b.properties()) return false;
        if (a.pauseMu() != b.pauseMu()) return false;
        if (a.phase() != b.phase()) return false;
        if (a.cause() != b.cause()) return false;

        return true;
    }

}
