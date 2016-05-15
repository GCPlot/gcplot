package com.gcplot.commons.enums;

import com.gcplot.commons.exceptions.Exceptions;
import gnu.trove.map.TIntObjectMap;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * http://stackoverflow.com/questions/2199399/storing-enumset-in-a-database
 *
 * But not relying on Enum.ordinal() value, used {@link TypedEnum} instead.
 */
public class EnumSetUtils {

    public static <E extends Enum<E> & TypedEnum> int encode(EnumSet<E> set) {
        int ret = 0;

        for (E val : set) {
            ret |= 1 << val.type();
        }

        return ret;
    }

    @SuppressWarnings("unchecked")
    public static <E extends Enum<E> & TypedEnum> EnumSet<E> decode(int code,
                                                         final Class<E> enumType) {
        try {
            TIntObjectMap<E> types = cachedTypes.computeIfAbsent(enumType,
                    k -> {
                        try {
                            return (TIntObjectMap<E>) enumType.getMethod("types").invoke(null);
                        } catch (Exception e) {
                            throw Exceptions.runtime(e);
                        }
                    });
            EnumSet<E> result = EnumSet.noneOf(enumType);
            while (code != 0) {
                int type = Integer.numberOfTrailingZeros(code);
                code ^= Integer.lowestOneBit(code);
                result.add(types.get(type));
            }
            return result;
        } catch (Exception e) {
            throw Exceptions.runtime(e);
        }
    }

    private static final Map<Class, TIntObjectMap> cachedTypes = new ConcurrentHashMap<>();
}
