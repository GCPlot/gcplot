package com.gcplot.services.network;

import com.gcplot.utils.enums.TypedEnum;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 10/22/17
 */
public enum ProxyType implements TypedEnum {

    NONE(0), SOCKS5(1), HTTP(2), HTTPS(3);

    private static final Int2ObjectMap<ProxyType> VALUES;

    static {
        Int2ObjectMap<ProxyType> types = new Int2ObjectOpenHashMap<>();
        for (ProxyType pt : values()) {
            types.put(pt.type(), pt);
        }
        VALUES = Int2ObjectMaps.unmodifiable(types);
    }

    private int type;

    ProxyType(int type) {
        this.type = type;
    }

    @Override
    public int type() {
        return type;
    }

    public static ProxyType get(int type) {
        return VALUES.get(type);
    }

    /**
     * Must be overlapped with concrete implementation.
     */
    public static Int2ObjectMap<ProxyType> types() {
        return null;
    }

}
