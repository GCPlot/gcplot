package com.gcplot.utils.enums;


import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public interface TypedEnum {

    int type();

    static <D extends Enum> D get(int type) {
        return null;
    }

    /**
     * Must be overlapped with concrete implementation.
     */
    static <D extends Enum> Int2ObjectMap<D> types() {
        return null;
    }

}
