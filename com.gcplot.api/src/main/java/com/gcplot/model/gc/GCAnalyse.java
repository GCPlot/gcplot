package com.gcplot.model.gc;

import com.gcplot.Identifier;
import com.gcplot.model.VMVersion;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.Set;

public interface GCAnalyse {

    String id();

    Identifier accountId();

    String name();

    boolean isContinuous();

    DateTime start();

    DateTime lastEvent();

    VMVersion vmVersion();

    GarbageCollectorType collectorType();

    Map<String, String> jvmHeaders();

    Set<String> jvmIds();

    Map<String, MemoryDetails> jvmMemoryDetails();

    String ext();

}
