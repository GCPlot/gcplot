package com.gcplot.model.gc;

public interface MemoryDetails {

    long pageSize();

    long physicalTotal();

    long physicalFree();

    long swapTotal();

    long swapFree();

}
