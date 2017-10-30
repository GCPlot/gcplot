package com.gcplot.model;

import com.gcplot.Identifier;
import com.gcplot.model.gc.*;
import com.gcplot.model.gc.analysis.GCAnalyse;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         2/25/17
 */
public class DefaultGCAnalyseFactory implements GCAnalyseFactory {
    @Override
    public GCAnalyse create(String id, Identifier accountId, String name, String tz, boolean isContinuous,
                            DateTime start, Set<String> jvmIds, Map<String, DateTime> lastEvent,
                            Map<String, DateTime> firstEvent, Map<String, String> jvmHeaders,
                            Map<String, String> jvmNames, Map<String, VMVersion> jvmVersions,
                            Map<String, GarbageCollectorType> jvmGCTypes, Map<String, MemoryDetails> jvmMemoryDetails,
                            SourceType sourceType, String sourceConfig, Map<String, SourceType> sourceByJvm,
                            Map<String, String> sourceConfigByJvm, String ext) {
        GCAnalyseImpl analyse = new GCAnalyseImpl();
        return analyse.id(id).accountId(accountId).name(name).timezone(tz).isContinuous(isContinuous)
                .start(start).jvmIds(jvmIds).lastEvent(lastEvent).firstEvent(firstEvent).jvmHeaders(jvmHeaders)
                .jvmNames(jvmNames).jvmVersions(jvmVersions).jvmGCTypes(jvmGCTypes).jvmMemoryDetails(jvmMemoryDetails)
                .sourceType(sourceType).sourceConfig(sourceConfig).sourceByJvm(sourceByJvm)
                .sourceConfigByJvm(sourceConfigByJvm).ext(ext);
    }

    @Override
    public GCAnalyse create(String id, Identifier accountId, String name, String tz, boolean isContinuous,
                            DateTime start, String ext) {
        GCAnalyseImpl analyse = new GCAnalyseImpl();
        return analyse.id(id).accountId(accountId).name(name).timezone(tz).isContinuous(isContinuous)
                .start(start).sourceType(SourceType.NONE).ext(ext);
    }

    @Override
    public GCAnalyse create(GCAnalyse analyse) {
        return new GCAnalyseImpl(analyse);
    }

    @Override
    public GCAnalyse create(GCAnalyse analyse, Set<String> jvmIds, Map<String, String> jvmNames,
                            Map<String, VMVersion> jvmVersions, Map<String, GarbageCollectorType> jvmGCTypes) {
        GCAnalyseImpl na = new GCAnalyseImpl(analyse);
        return na.jvmIds(jvmIds).jvmNames(jvmNames).jvmVersions(jvmVersions).jvmGCTypes(jvmGCTypes);
    }
}
