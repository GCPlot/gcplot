package com.gcplot.logs;

import com.gcplot.Identifier;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/14/17
 */
public class LogHandle {
    private final String name;
    private final Identifier accountId;
    private final String analyzeId;
    private final String jvmId;

    public String getName() {
        return name;
    }

    public Identifier getAccountId() {
        return accountId;
    }

    public String getAnalyzeId() {
        return analyzeId;
    }

    public String getJvmId() {
        return jvmId;
    }

    public LogHandle(String name, Identifier accountId, String analyzeId, String jvmId) {
        this.name = name;
        this.accountId = accountId;
        this.analyzeId = analyzeId;
        this.jvmId = jvmId;
    }
}
