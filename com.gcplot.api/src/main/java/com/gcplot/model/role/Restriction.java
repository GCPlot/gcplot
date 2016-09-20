package com.gcplot.model.role;

import java.util.Map;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/14/16
 */
public interface Restriction {

    /**
     * The type of the restriction.
     */
    RestrictionType type();

    /**
     * The identifier of the action restriction should be
     * applied on.
     */
    String action();

    /**
     * Restriction for the TOGGLE type.
     */
    boolean restricted();

    /**
     * Restriction on amount for QUANTITATIVE type.
     */
    long amount();

    /**
     * Restrictive properties for PROPERTY_BASED type.
     */
    Map<String, String> properties();

}
