package com.gcplot.model.role;

import java.util.Map;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/14/16
 */
public interface Restriction {

    RestrictionType type();

    String action();

    long amount();

    Map<String, String> properties();

}
