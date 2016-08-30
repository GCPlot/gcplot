package com.gcplot.repository;

import com.gcplot.Identifier;
import com.gcplot.model.VMVersion;
import com.gcplot.model.gc.GCAnalyse;
import com.gcplot.model.gc.GarbageCollectorType;
import com.gcplot.model.gc.MemoryDetails;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

public interface GCAnalyseRepository {

    List<GCAnalyse> analyses();

    OptionalLong analysesCount(Identifier accountId);

    Optional<GCAnalyse> analyse(String id);

    List<GCAnalyse> analysesFor(Identifier accountId);

    /**
     * Adds new analyse to the system.
     *
     * @param analyse to add
     * @return new analyse id
     */
    String newAnalyse(GCAnalyse analyse);

    void addJvm(Identifier accountId, String analyseId,
                String jvmId, VMVersion version, GarbageCollectorType type,
                String headers, MemoryDetails memoryDetails);

    void updateJvmInfo(Identifier accountId, String analyseId,
                       String jvmId, String headers, MemoryDetails memoryDetails);

    void updateJvmVersion(Identifier accountId, String analyseId,
                          String jvmId, VMVersion version, GarbageCollectorType type);

    void removeJvm(Identifier accountId, String analyseId, String jvmId);

    void removeAnalyse(Identifier accountId, String analyseId);

    void updateLastEvent(Identifier accountId, String analyseId, DateTime lastEvent);

}
