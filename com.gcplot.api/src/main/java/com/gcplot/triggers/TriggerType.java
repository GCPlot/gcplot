package com.gcplot.triggers;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/1/17
 */
public enum TriggerType {

    IS_GCPC_WORK(1);

    private static final Int2ObjectMap<TriggerType> VALUES;

    static {
        Int2ObjectMap<TriggerType> values = new Int2ObjectOpenHashMap<>();
        for (TriggerType tt : values()) {
            values.put(tt.getId(), tt);
        }
        VALUES = values;
    }

    private int id;
    public int getId() {
        return id;
    }

    TriggerType(int id) {
        this.id = id;
    }

    public static TriggerType by(int id) {
        return VALUES.get(id);
    }
}
