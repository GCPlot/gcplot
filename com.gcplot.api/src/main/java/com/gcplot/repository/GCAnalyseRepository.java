package com.gcplot.repository;

import com.gcplot.Identifier;
import com.gcplot.model.gc.analysis.ConfigProperty;
import com.gcplot.model.gc.analysis.GCAnalyse;
import com.gcplot.repository.operations.analyse.AnalyseOperation;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

public interface GCAnalyseRepository {

    List<GCAnalyse> analyses();

    Iterable<GCAnalyse> analyses(boolean isContinuous);

    OptionalLong analysesCount(Identifier accountId);

    Optional<GCAnalyse> analyse(Identifier accountId, String id);

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

    void updateConfig(GCAnalyse analyse, ConfigProperty cp, String val);

}
