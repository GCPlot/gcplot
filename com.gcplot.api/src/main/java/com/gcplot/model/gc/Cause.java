package com.gcplot.model.gc;

import com.gcplot.commons.enums.TypedEnum;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         12/22/16
 */
public enum Cause implements TypedEnum {
    SYSTEM_GC(1), ALLOCATION_PROFILER(2), JVMTI_ENV(3), GC_LOCKER(4), HEAP_INSPECTION(5),
    HEAP_DUMP(6), NO_GC(7), ALLOCATION_FAILURE(8), PERM_GENERATION_FULL(9), METADATA_GC_THRESHOLD(10),
    CMS_INITIAL_MARK(11), CMS_FINAL_REMARK(12), ADAPTIVE_SIZE_ERGONOMICS(13), G1_EVACUATION_PAUSE(14),
    G1_HUMONGOUS_ALLOCATION(15), LAST_DITCH_COLLECTION(16), OTHER(0);

    private int type;
    private static Int2ObjectMap<Cause> types = new Int2ObjectOpenHashMap<>();

    @Override
    public int type() {
        return type;
    }

    public static Cause get(int type) {
        return types.get(type);
    }

    public static Int2ObjectMap<Cause> types() {
        return types;
    }

    Cause(int type) {
        this.type = type;
    }

    static {
        for (Cause g : Cause.values()) {
            types.put(g.type, g);
        }
    }
}
