package com.gcplot.commons.enums;

import gnu.trove.map.TIntObjectMap;

public interface TypedEnum {

    int type();

    /**
     * Must be overlapped with concrete implementation.
     */
    static TIntObjectMap<Enum> types() {
        return null;
    }

}
