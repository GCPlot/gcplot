package com.gcplot.model.gc;

import com.gcplot.Identifier;
import org.joda.time.DateTime;

import java.util.List;

public interface GCAnalyse {

    long id();

    Identifier accountId();

    String name();

    boolean isContinuous();

    DateTime start();

    DateTime lastEvent();

    GarbageCollectorType collectorType();

    List<String> commandLineParams();

    String header();

    MemoryDetails memoryDetails();

}
