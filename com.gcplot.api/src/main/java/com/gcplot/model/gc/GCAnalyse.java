package com.gcplot.model.gc;

import com.gcplot.Identifier;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GCAnalyse {

    String id();

    Identifier accountId();

    String name();

    boolean isContinuous();

    DateTime start();

    DateTime lastEvent();

    GarbageCollectorType collectorType();

    Map<String, String> jvmHeaders();

    Set<String> jvmIds();

    Map<String, MemoryDetails> jvmMemoryDetails();

}
