package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.model.gc.ObjectsAges;
import org.joda.time.DateTimeZone;

import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         9/17/16
 */
public class ObjectsAgesResponse {
    @JsonProperty("time")
    public long time;
    @JsonProperty("occupied")
    public List<Long> occupied;
    @JsonProperty("total")
    public List<Long> total;
    @JsonProperty("ext")
    public String ext;

    public ObjectsAgesResponse(@JsonProperty("time") long time,
                               @JsonProperty("occupied") List<Long> occupied,
                               @JsonProperty("total") List<Long> total,
                               @JsonProperty("ext") String ext) {
        this.time = time;
        this.occupied = occupied;
        this.total = total;
        this.ext = ext;
    }

    public static ObjectsAgesResponse from(ObjectsAges oa) {
        return new ObjectsAgesResponse(oa.occurred().toDateTime(DateTimeZone.UTC).getMillis(),
                oa.occupied(), oa.total(), oa.ext());
    }
}
