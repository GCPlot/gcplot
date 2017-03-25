package com.gcplot.resource;

import java.io.File;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         9/10/16
 */
public interface ResourceManager {

    void upload(File file, String newPath, String contentType);

    boolean isDisabled();

    default void upload(File file, String newPath) {
        upload(file, newPath, null);
    }

}
