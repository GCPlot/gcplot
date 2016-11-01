package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.model.gc.Capacity;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         9/12/16
 */
public class CapacityResponse {
    @JsonProperty("b")
    public long usedBefore;
    @JsonProperty("a")
    public long usedAfter;
    @JsonProperty("t")
    public long total;

    public CapacityResponse(@JsonProperty("b") long usedBefore,
                            @JsonProperty("a") long usedAfter,
                            @JsonProperty("t") long total) {
        this.usedBefore = usedBefore;
        this.usedAfter = usedAfter;
        this.total = total;
    }

    public static String toJson(Capacity capacity) {
        return "{\"b\":" + capacity.usedBefore() + ",\"a\":" + capacity.usedAfter() + ",\"t\":" + capacity.total() + "}";
    }

    public static CapacityResponse from(Capacity capacity) {
        if (capacity == null) {
            return null;
        }
        return new CapacityResponse(capacity.usedBefore(), capacity.usedAfter(), capacity.total());
    }
}
