package com.gcplot.model.gc;

import com.gcplot.Identifier;
import com.gcplot.model.VMVersion;
import com.gcplot.model.gc.analysis.GCAnalyse;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         2/25/17
 */
public interface GCAnalyseFactory {

    GCAnalyse create(String id, Identifier accountId, String name, String tz, boolean isContinuous, DateTime start,
                     Set<String> jvmIds, Map<String, DateTime> lastEvent, Map<String, DateTime> firstEvent,
                     Map<String, String> jvmHeaders, Map<String, String> jvmNames, Map<String, VMVersion> jvmVersions,
                     Map<String, GarbageCollectorType> jvmGCTypes, Map<String, MemoryDetails> jvmMemoryDetails,
                     SourceType sourceType, String sourceConfig, Map<String, SourceType> sourceByJvm,
                     Map<String, String> sourceConfigByJvm, String ext);

    GCAnalyse create(String id, Identifier accountId, String name, String tz, boolean isContinuous, DateTime start,
                     String ext);

    GCAnalyse create(GCAnalyse analyse);

    GCAnalyse create(GCAnalyse analyse, Set<String> jvmIds, Map<String, String> jvmNames,
                     Map<String, VMVersion> jvmVersions, Map<String, GarbageCollectorType> jvmGCTypes);

}
