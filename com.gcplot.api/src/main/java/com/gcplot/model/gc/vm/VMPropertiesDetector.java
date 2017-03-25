package com.gcplot.model.gc.vm;

import com.gcplot.logs.LogSource;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         2/25/17
 */
public interface VMPropertiesDetector {

    VMProperties detect(LogSource source);

}
