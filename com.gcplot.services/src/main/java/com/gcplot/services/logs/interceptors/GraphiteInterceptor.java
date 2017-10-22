package com.gcplot.services.logs.interceptors;

import com.gcplot.logs.EventInterceptor;
import com.gcplot.model.gc.GCAnalyse;
import com.gcplot.model.gc.GCEvent;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 10/22/17
 */
public class GraphiteInterceptor implements EventInterceptor {

    @Override
    public void intercept(GCAnalyse analyse, String jvmId, GCEvent gcEvent) {
        
    }

}
