package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/22/16
 */
public class NewAnalyseResponse {
    @JsonProperty("id")
    public String id;

    public NewAnalyseResponse(@JsonProperty("id") String id) {
        this.id = id;
    }

}
