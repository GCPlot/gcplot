package com.gcplot.services.mail.data;

import com.gcplot.model.gc.GCAnalyse;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.Set;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 10/8/17
 */
public class JvmAgentStatus {
    private final GCAnalyse analysis;
    private final Set<Pair<String, Long>> jvms;

    public JvmAgentStatus(GCAnalyse analysis, Set<Pair<String, Long>> jvms) {
        this.analysis = analysis;
        this.jvms = Collections.unmodifiableSet(jvms);
    }

    public GCAnalyse getAnalysis() {
        return analysis;
    }

    public Set<Pair<String, Long>> getJvms() {
        return jvms;
    }
}
