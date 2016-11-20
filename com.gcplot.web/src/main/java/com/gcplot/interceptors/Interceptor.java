package com.gcplot.interceptors;

import com.gcplot.model.gc.GCEvent;
import com.gcplot.web.RequestContext;

import java.util.function.Consumer;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         11/18/16
 */
public interface Interceptor<T> {

    void process(GCEvent event, Runnable delimit, RequestContext ctx);

    void complete(Runnable delimit, RequestContext ctx);

}
