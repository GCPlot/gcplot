package com.gcplot.model.gc;

import com.gcplot.commons.enums.TypedEnum;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public enum GarbageCollectorType implements TypedEnum {

    ORACLE_SERIAL(1),
    ORACLE_OLD_GC(2),
    ORACLE_PAR_OLD_GC(3),
    ORACLE_CMS(4),
    ORACLE_G1(5);

    private int type;
    private static TIntObjectMap<GarbageCollectorType> types = new TIntObjectHashMap<>();

    @Override
    public int type() {
        return type;
    }

    public static GarbageCollectorType get(int type) {
        return types.get(type);
    }

    public static TIntObjectMap<GarbageCollectorType> types() {
        return types;
    }

    GarbageCollectorType(int type) {
        this.type = type;
    }

    static {
        for (GarbageCollectorType g : GarbageCollectorType.values()) {
            types.put(g.type, g);
        }
    }
}
