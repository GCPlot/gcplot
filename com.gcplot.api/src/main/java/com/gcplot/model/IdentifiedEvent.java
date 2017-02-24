package com.gcplot.model;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         2/24/17
 */
public interface IdentifiedEvent {

    default boolean isGCEvent() {
        return false;
    }

    default boolean isRate() {
        return false;
    }

    default boolean isStatistic() {
        return false;
    }

}
