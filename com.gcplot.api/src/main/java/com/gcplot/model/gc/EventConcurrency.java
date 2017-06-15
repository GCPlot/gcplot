package com.gcplot.model.gc;

import com.gcplot.utils.enums.TypedEnum;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public enum EventConcurrency implements TypedEnum {

    CONCURRENT(1),
    SERIAL(2);

    private int type;
    private static Int2ObjectMap<EventConcurrency> types = new Int2ObjectOpenHashMap<>();

    EventConcurrency(int type) {
        this.type = type;
    }

    @Override
    public int type() {
        return type;
    }

    public static EventConcurrency get(int type) {
        return types.get(type);
    }

    public static Int2ObjectMap<EventConcurrency> types() {
        return types;
    }

    static {
        for (EventConcurrency g : EventConcurrency.values()) {
            types.put(g.type, g);
        }
    }
}
