package com.gcplot.repository;

import com.gcplot.Identifier;
import com.gcplot.model.VMVersion;
import com.gcplot.model.gc.GCAnalyse;
import com.gcplot.model.gc.GarbageCollectorType;
import com.gcplot.model.gc.MemoryDetails;
import com.gcplot.repository.operations.analyse.AnalyseOperation;
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

    void perform(AnalyseOperation operation);

    void perform(List<AnalyseOperation> operations);

}
