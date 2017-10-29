package com.gcplot.logs;

import com.gcplot.model.gc.analysis.GCAnalyse;

import java.util.List;

public interface IdentifiedEventInterceptorFactory {

    List<IdentifiedEventInterceptor> create(GCAnalyse analyse, String jvmId);

}
