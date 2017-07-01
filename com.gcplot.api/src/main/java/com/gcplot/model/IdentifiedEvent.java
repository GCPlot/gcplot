package com.gcplot.model;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         2/24/17
 */
public interface IdentifiedEvent {

    default boolean isGCEvent() {
        return false;
    }

    default boolean isGCRate() {
        return false;
    }

    default boolean isStatistic() {
        return false;
    }

    default boolean isPauseRate() {
        return false;
    }

}
