package com.gcplot.commons;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/31/16
 */
public abstract class CollectionUtils {
    private static final IntList EMPTY_INT_LIST = IntLists.unmodifiable(new IntArrayList());
    private static final IntSet EMPTY_INT_SET = IntSets.unmodifiable(new IntOpenHashSet());
    private static final LongList EMPTY_LONG_LIST = LongLists.unmodifiable(new LongArrayList());
    private static final LongSet EMPTY_LONG_SET = LongSets.unmodifiable(new LongOpenHashSet());

    public static IntList emptyIntList() {
        return EMPTY_INT_LIST;
    }

    public static IntSet emptyIntSet() {
        return EMPTY_INT_SET;
    }

    public static LongList emptyLongList() {
        return EMPTY_LONG_LIST;
    }

    public static LongSet emptyLogSet() {
        return EMPTY_LONG_SET;
    }

    public static IntSet copy(IntSet set) {
        return new IntOpenHashSet(set);
    }

    public static LongSet copy(LongSet set) {
        return new LongOpenHashSet(set);
    }

    public static <K, V1, V2> Map<K, V2> transformValue(Map<K, V1> map, Function<V1, V2> valueMapper) {
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                e -> valueMapper.apply(e.getValue())));
    }

    public static <K1, K2, V1, V2> Map<K2, V2> transformKeyValue(Map<K1, V1> map, Function<K1, K2> keyMapper,
                                                                 Function<V1, V2> valueMapper) {
        return map.entrySet().stream().collect(Collectors.toMap(e -> keyMapper.apply(e.getKey()),
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

    public static <K, V1, V2> Map<K, V2> processMap(Map<K, V1> map, Function<? super V1, ? extends V2> valueMapper) {
        return map != null && map.size() > 0 ? transformValue(map, valueMapper::apply) : Collections.emptyMap();
    }

    public static <K1, K2, V1, V2> Map<K2, V2> processKeyMap(Map<K1, V1> map,
                                                             Function<K1, K2> keyMapper,
                                                             Function<? super V1, ? extends V2> valueMapper) {
        return map != null && map.size() > 0 ? transformKeyValue(map, keyMapper, valueMapper::apply) : Collections.emptyMap();
    }

}
