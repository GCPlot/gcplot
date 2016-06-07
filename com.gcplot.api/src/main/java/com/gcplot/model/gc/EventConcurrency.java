package com.gcplot.model.gc;

import com.gcplot.commons.enums.TypedEnum;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public enum EventConcurrency implements TypedEnum {

    CONCURRENT(1),
    SERIAL(2);

    private int type;
    private static TIntObjectMap<EventConcurrency> types = new TIntObjectHashMap<>();

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

    public static TIntObjectMap<EventConcurrency> types() {
        return types;
    }

    static {
        for (EventConcurrency g : EventConcurrency.values()) {
            types.put(g.type, g);
        }
    }
}
