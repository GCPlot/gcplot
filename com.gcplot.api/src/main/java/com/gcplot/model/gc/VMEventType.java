package com.gcplot.model.gc;

import com.gcplot.utils.enums.TypedEnum;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public enum VMEventType implements TypedEnum {

    GARBAGE_COLLECTION(1),
    STW_NON_GC(2);

    private int type;
    private static Int2ObjectMap<VMEventType> types = new Int2ObjectOpenHashMap<>();

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

    public static Int2ObjectMap<VMEventType> types() {
        return types;
    }

    static {
        for (VMEventType g : VMEventType.values()) {
            types.put(g.type, g);
        }
    }
}
