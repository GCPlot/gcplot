package com.gcplot.model.gc;

import com.fasterxml.jackson.annotation.JsonValue;
import com.gcplot.commons.enums.TypedEnum;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         11/2/16
 */
public enum Phase implements TypedEnum {
    /**
     * This is a stop the world event. With G1, it is piggybacked on a normal young GC.
     * Mark survivor regions (root regions) which may have references to objects in old generation.
     */
    G1_INITIAL_MARK(1),
    /**
     * Scan survivor regions for references into the old generation.
     * This happens while the application continues to run.
     * The phase must be completed before a young GC can occur.
     */
    G1_ROOT_REGION_SCANNING(2),
    /**
     * Find live objects over the entire heap.
     * This happens while the application is running.
     * This phase can be interrupted by young generation garbage collections.
     */
    G1_CONCURRENT_MARKING(3),
    /**
     * Completes the marking of live object in the heap.
     * Uses an algorithm called snapshot-at-the-beginning (SATB) which is much faster than what was used in
     * the CMS collector.
     */
    G1_REMARK(4),
    /**
     * - Performs accounting on live objects and completely free regions. (Stop the world)
     * - Scrubs the Remembered Sets. (Stop the world)
     * - Reset the empty regions and return them to the free list. (Concurrent)
     */
    G1_CLEANUP(5),
    /**
     * These are the stop the world pauses to evacuate or copy live objects to new unused regions.
     * This can be done with young generation regions which are logged as [GC pause (young)].
     * Or both young and old generation regions which are logged as [GC Pause (mixed)].
     */
    G1_COPYING(6),
    /**
     * This is the the start of CMS collection. This is a stop-the-world phase.
     */
    CMS_INITIAL_MARK(7),
    /**
     * This is a concurrent phase. CMS restarts all the applications threads and
     * then starting from the objects found in 1), it finds all the other live objects.
     */
    CMS_CONCURRENT_MARK(8),
    /**
     * This is a concurrent phase. This phase is an optimization.
     */
    CMS_CONCURRENT_PRECLEAN(9),
    /**
     * The remark phase is a stop-the-world. CMS cannot correctly determine which objects are alive (mark them live),
     * if the application is running concurrently and keeps changing what is live.
     */
    CMS_REMARK(10),
    /**
     * This is a concurrent phase. CMS looks at all the objects and adds newly dead objects to its freelists.
     */
    CMS_CONCURRENT_SWEEP(11),
    /**
     * This is a concurrent phase. CMS is cleaning up its state so that it is ready for the next collection.
     */
    CMS_CONCURRENT_RESET(12),
    OTHER(0);

    private int type;
    private static Int2ObjectMap<Phase> types = new Int2ObjectOpenHashMap<>();

    @Override
    public int type() {
        return type;
    }

    public static Phase get(int type) {
        return types.get(type);
    }

    public static Int2ObjectMap<Phase> types() {
        return types;
    }

    Phase(int type) {
        this.type = type;
    }

    @Override
    @JsonValue
    public String toString() {
        return Integer.toString(type);
    }

    static {
        for (Phase g : Phase.values()) {
            types.put(g.type, g);
        }
    }
}
