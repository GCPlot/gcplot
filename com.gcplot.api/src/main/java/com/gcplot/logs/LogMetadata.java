package com.gcplot.logs;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/6/16
 */
public class LogMetadata {

    private long pageSize;
    public long pageSize() {
        return pageSize;
    }
    public LogMetadata pageSize(long pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    private long physicalTotal;
    public long physicalTotal() {
        return physicalTotal;
    }
    public LogMetadata physicalTotal(long physicalTotal) {
        this.physicalTotal = physicalTotal;
        return this;
    }

    private long physicalFree;
    public long physicalFree() {
        return physicalFree;
    }
    public LogMetadata physicalFree(long physicalFree) {
        this.physicalFree = physicalFree;
        return this;
    }

    private long swapFree;
    public long swapFree() {
        return swapFree;
    }
    public LogMetadata swapFree(long swapFree) {
        this.swapFree = swapFree;
        return this;
    }

    private long swapTotal;
    public long swapTotal() {
        return swapTotal;
    }
    public LogMetadata swapTotal(long swapTotal) {
        this.swapTotal = swapTotal;
        return this;
    }

    private String commandLines;
    public String commandLines() {
        return commandLines;
    }
    public LogMetadata commandLines(String commandLines) {
        this.commandLines = commandLines;
        return this;
    }

    @Override
    public String toString() {
        return "LogMetadata{" +
                "pageSize=" + pageSize +
                ", physicalTotal=" + physicalTotal +
                ", physicalFree=" + physicalFree +
                ", swapFree=" + swapFree +
                ", swapTotal=" + swapTotal +
                ", commandLines='" + commandLines + '\'' +
                '}';
    }
}
