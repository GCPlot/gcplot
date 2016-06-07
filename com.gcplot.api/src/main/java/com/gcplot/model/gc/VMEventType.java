package com.gcplot.model.gc;

import com.gcplot.commons.enums.TypedEnum;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public enum VMEventType implements TypedEnum {

    GARBAGE_COLLECTION(1),
    STW_NON_GC(2);

    private int type;
    private static TIntObjectMap<VMEventType> types = new TIntObjectHashMap<>();

    VMEventType(int type) {
        this.type = type;
    }

    @Override
    public int type() {
        return type;
    }

    public static VMEventType get(int type) {
        return types.get(type);
    }

    public static TIntObjectMap<VMEventType> types() {
        return types;
    }

    static {
        for (VMEventType g : VMEventType.values()) {
            types.put(g.type, g);
        }
    }
}
