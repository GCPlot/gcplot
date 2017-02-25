package com.gcplot.model;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/3/16
 */
public interface VMEvent {

    String jvmId();

    VMEvent jvmId(String jvmId);

    String analyseId();

    VMEvent analyseId(String analyseId);
}
