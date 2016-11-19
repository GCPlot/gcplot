package com.gcplot.commons.enums;

import com.gcplot.commons.exceptions.Exceptions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * http://stackoverflow.com/questions/2199399/storing-enumset-in-a-database
 *
 * But not relying on Enum.ordinal() value, used {@link TypedEnum} instead.
 */
public class EnumSetUtils {

    public static <E extends Enum<E> & TypedEnum> long encode(EnumSet<E> set) {
        long ret = 0;

        for (E val : set) {
            ret |= 1 << val.type();
        }

        return ret;
    }

    @SuppressWarnings("unchecked")
    public static <E extends Enum<E> & TypedEnum> EnumSet<E> decode(long code,
                                                         final Class<E> enumType) {
        try {
            Int2ObjectMap<E> types = cachedTypes.computeIfAbsent(enumType,
                    k -> {
                        try {
                            return (Int2ObjectMap<E>) enumType.getMethod("types").invoke(null);
                        } catch (Exception e) {
                            throw Exceptions.runtime(e);
                        }
                    });
            EnumSet<E> result = EnumSet.noneOf(enumType);
            while (code != 0) {
                int type = Long.numberOfTrailingZeros(code);
                code ^= Long.lowestOneBit(code);
                result.add(types.get(type));
            }
            return result;
        } catch (Exception e) {
            throw Exceptions.runtime(e);
        }
    }

    private static final Map<Class, Int2ObjectMap> cachedTypes = new ConcurrentHashMap<>();
}
