package com.gcplot.model.gc;

import com.gcplot.commons.enums.TypedEnum;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public enum Generation implements TypedEnum {

    YOUNG(1),
    TENURED(2),
    /* Used before Java 8 */
    PERM(3),
    /* Used after Java 8 */
    METASPACE(4),
    OLD(5),
    OTHER(6);

    private int type;
    private static Int2ObjectMap<Generation> types = new Int2ObjectOpenHashMap<>();

    @Override
    public int type() {
        return type;
    }

    public static Generation get(int type) {
        return types.get(type);
    }

    public static Int2ObjectMap<Generation> types() {
        return types;
    }

    Generation(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return Integer.toString(type);
    }

    static {
        for (Generation g : Generation.values()) {
            types.put(g.type, g);
        }
    }
}
