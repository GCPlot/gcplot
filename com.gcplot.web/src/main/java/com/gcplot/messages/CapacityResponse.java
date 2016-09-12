package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.model.gc.Capacity;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         9/12/16
 */
public class CapacityResponse {
    @JsonProperty("ub")
    public long usedBefore;
    @JsonProperty("ua")
    public long usedAfter;
    @JsonProperty("ttl")
    public long total;

    public CapacityResponse(@JsonProperty("ub") long usedBefore,
                            @JsonProperty("ua") long usedAfter,
                            @JsonProperty("ttl") long total) {
        this.usedBefore = usedBefore;
        this.usedAfter = usedAfter;
        this.total = total;
    }

    public static CapacityResponse from(Capacity capacity) {
        if (capacity == null) {
            return null;
        }
        return new CapacityResponse(capacity.usedBefore(), capacity.usedAfter(), capacity.total());
    }
}
