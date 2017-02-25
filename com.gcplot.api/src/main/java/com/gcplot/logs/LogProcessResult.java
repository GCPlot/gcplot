package com.gcplot.logs;

import com.google.common.base.Strings;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         2/25/17
 */
public class LogProcessResult {
    public static final LogProcessResult SUCCESS = new LogProcessResult();
    private final String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isSuccess() {
        return Strings.isNullOrEmpty(errorMessage);
    }

    public LogProcessResult() {
        this.errorMessage = null;
    }

    public LogProcessResult(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
