package com.gcplot.commons.enums;

import gnu.trove.map.TIntObjectMap;

public interface TypedEnum {

    int type();

    static <D extends Enum> D get(int type) {
        return null;
    }

    /**
     * Must be overlapped with concrete implementation.
     */
    static <D extends Enum> TIntObjectMap<D> types() {
        return null;
    }

}
