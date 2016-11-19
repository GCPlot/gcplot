package com.gcplot.interceptors;

import com.gcplot.model.gc.GCEvent;
import com.gcplot.web.RequestContext;

import java.util.function.Consumer;

/**
 * Promotion and allocation rates interceptor which helps gather
 * time-based plot data. Depends on some time window.
 *
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         11/18/16
 */
public class RatesInterceptor implements Interceptor {

    @Override
    public void process(GCEvent event, RequestContext ctx) {

    }

    @Override
    public void complete(RequestContext ctx) {

    }
}
