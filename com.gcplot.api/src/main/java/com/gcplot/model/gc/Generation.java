package com.gcplot.model.gc;

import com.gcplot.commons.enums.TypedEnum;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

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
    private static TIntObjectMap<Generation> types = new TIntObjectHashMap<>();

    @Override
    public int type() {
        return type;
    }

    public static Generation get(int type) {
        return types.get(type);
    }

    public static TIntObjectMap<Generation> types() {
        return types;
    }

    Generation(int type) {
        this.type = type;
    }

    static {
        for (Generation g : Generation.values()) {
            types.put(g.type, g);
        }
    }
}
