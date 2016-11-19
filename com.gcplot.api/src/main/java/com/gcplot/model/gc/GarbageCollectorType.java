package com.gcplot.model.gc;

import com.gcplot.commons.enums.TypedEnum;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public enum GarbageCollectorType implements TypedEnum {

    ORACLE_SERIAL(1),
    ORACLE_OLD_GC(2),
    ORACLE_PAR_OLD_GC(3),
    ORACLE_CMS(4),
    ORACLE_G1(5);

    private int type;
    private static Int2ObjectMap<GarbageCollectorType> types = new Int2ObjectOpenHashMap<>();

    @Override
    public int type() {
        return type;
    }

    public static GarbageCollectorType get(int type) {
        return types.get(type);
    }

    public static Int2ObjectMap<GarbageCollectorType> types() {
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
