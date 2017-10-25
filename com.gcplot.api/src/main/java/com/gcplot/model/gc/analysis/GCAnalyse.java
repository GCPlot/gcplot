package com.gcplot.model.gc.analysis;

import com.gcplot.Identifier;
import com.gcplot.model.VMVersion;
import com.gcplot.model.config.Configuration;
import com.gcplot.model.gc.GarbageCollectorType;
import com.gcplot.model.gc.MemoryDetails;
import com.gcplot.model.gc.SourceType;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.Properties;
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

    Map<String, DateTime> firstEvent();

    Map<String, String> jvmHeaders();

    Map<String, String> jvmNames();

    Map<String, VMVersion> jvmVersions();

    Map<String, GarbageCollectorType> jvmGCTypes();

    Map<String, MemoryDetails> jvmMemoryDetails();

    SourceType sourceType();

    String sourceConfig();

    Properties sourceConfigProps();

    Map<String, SourceType> sourceByJvm();

    Map<String, String> sourceConfigByJvm();

    Configuration<ConfigProperty> config();

    String ext();

}
