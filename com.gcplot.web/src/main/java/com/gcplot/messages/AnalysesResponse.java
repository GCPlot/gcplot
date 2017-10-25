package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.model.gc.analysis.GCAnalyse;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/23/16
 */
public class AnalysesResponse {
    @JsonProperty("analyses")
    public List<AnalyseResponse> analyses;

    public AnalysesResponse(Collection<GCAnalyse> analyses) {
        this.analyses = analyses.stream().map(AnalyseResponse::new).collect(Collectors.toList());
    }

}
