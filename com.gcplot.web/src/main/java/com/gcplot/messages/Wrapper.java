package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Wrapper {

    @JsonProperty("result")
    private Object result;
    public Object getResult() {
        return result;
    }

    public Wrapper(Object result) {
        this.result = result;
    }

}
