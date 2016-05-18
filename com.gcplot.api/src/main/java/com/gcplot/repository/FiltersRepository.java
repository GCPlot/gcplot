package com.gcplot.repository;

import java.util.List;

public interface FiltersRepository {

    boolean isFiltered(String type, String value);

    void filter(String type, String value);

    void notFilter(String type, String value);

    List<String> getAllFiltered(String type);

}
