package com.gcplot.commons;

import gnu.trove.impl.unmodifiable.TUnmodifiableIntList;
import gnu.trove.impl.unmodifiable.TUnmodifiableIntSet;
import gnu.trove.impl.unmodifiable.TUnmodifiableLongList;
import gnu.trove.impl.unmodifiable.TUnmodifiableLongSet;
import gnu.trove.list.TIntList;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/31/16
 */
public abstract class CollectionUtils {
    private static final TIntList EMPTY_INT_LIST = new TUnmodifiableIntList(new TIntArrayList());
    private static final TIntSet EMPTY_INT_SET = new TUnmodifiableIntSet(new TIntHashSet());
    private static final TLongList EMPTY_LONG_LIST = new TUnmodifiableLongList(new TLongArrayList());
    private static final TLongSet EMPTY_LONG_SET = new TUnmodifiableLongSet(new TLongHashSet());

    public static TIntList emptyIntList() {
        return EMPTY_INT_LIST;
    }

    public static TIntSet emptyIntSet() {
        return EMPTY_INT_SET;
    }

    public static TLongList emptyLongList() {
        return EMPTY_LONG_LIST;
    }

    public static TLongSet emptyLogSet() {
        return EMPTY_LONG_SET;
    }

    public static TIntList copy(TIntList list) {
        return list.subList(0, list.size());
    }

    public static TIntSet copy(TIntSet set) {
        return new TIntHashSet(set);
    }

    public static TLongList copy(TLongList list) {
        return list.subList(0, list.size());
    }

    public static TLongSet copy(TLongSet set) {
        return new TLongHashSet(set);
    }

    public static <K, V1, V2> Map<K, V2> transformValue(Map<K, V1> map, Function<V1, V2> valueMapper) {
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                e -> valueMapper.apply(e.getValue())));
    }

    public static <T> Set<T> cloneAndAdd(Set<T> old, T element) {
        Set<T> set = new HashSet<>(old);
        set.add(element);
        return set;
    }

    public static <K, V> Map<K, V> cloneAndPut(Map<K, V> old, K key, V value) {
        Map<K, V> map = new HashMap<>(old);
        map.put(key, value);
        return map;
    }

}
