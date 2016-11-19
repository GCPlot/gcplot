package com.gcplot.model;

import com.gcplot.commons.enums.TypedEnum;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/10/16
 */
public enum VMVersion implements TypedEnum {
    HOTSPOT_1_2_2(1),
    HOTSPOT_1_3_1(2),
    HOTSPOT_1_4(3),
    HOTSPOT_1_5(4),
    HOTSPOT_1_6(5),
    HOTSPOT_1_7(6),
    HOTSPOT_1_8(7),
    HOTSPOT_1_9(8);

    private int type;
    private static Int2ObjectMap<VMVersion> types = new Int2ObjectOpenHashMap<>();

    @Override
    public int type() {
        return type;
    }

    public static VMVersion get(int type) {
        return types.get(type);
    }

    public static Int2ObjectMap<VMVersion> types() {
        return types;
    }

    VMVersion(int type) {
        this.type = type;
    }

    static {
        for (VMVersion g : VMVersion.values()) {
            types.put(g.type, g);
        }
    }
}
