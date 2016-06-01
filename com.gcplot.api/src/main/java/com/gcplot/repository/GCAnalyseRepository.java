package com.gcplot.repository;

import com.gcplot.Identifier;
import com.gcplot.model.gc.GCAnalyse;
import com.gcplot.model.gc.MemoryDetails;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Optional;

public interface GCAnalyseRepository {

    List<GCAnalyse> analyses();

    Optional<GCAnalyse> analyse(String id);

    List<GCAnalyse> analysesFor(Identifier accountId);

    void newAnalyse(GCAnalyse analyse);

    void analyseJvm(Identifier accountId, String id, String jvmId, String headers, MemoryDetails memoryDetails);

    void removeAnalyse(Identifier accountId, String id);

    void updateLastEvent(Identifier accountId, String id, DateTime lastEvent);

}
