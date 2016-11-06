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

    String timezone();

    boolean isContinuous();

    DateTime start();

    Set<String> jvmIds();

    Map<String, DateTime> lastEvent();

    Map<String, String> jvmHeaders();

    Map<String, String> jvmNames();

    Map<String, VMVersion> jvmVersions();

    Map<String, GarbageCollectorType> jvmGCTypes();

    Map<String, MemoryDetails> jvmMemoryDetails();

    String ext();

}
