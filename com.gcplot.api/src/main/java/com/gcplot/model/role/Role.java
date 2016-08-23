package com.gcplot.model.role;

import com.gcplot.Identifier;

import java.util.List;

public interface Role {

    Identifier id();

    String title();

    List<Restriction> restrictions();

    boolean isDefault();

}
