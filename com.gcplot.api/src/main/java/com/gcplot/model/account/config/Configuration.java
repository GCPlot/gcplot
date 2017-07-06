package com.gcplot.model.account.config;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/3/17
 */
public interface Configuration {

    long asLong(ConfigProperty cp);

    double asDouble(ConfigProperty cp);

    boolean asBoolean(ConfigProperty cp);

    String asString(ConfigProperty cp);

}
