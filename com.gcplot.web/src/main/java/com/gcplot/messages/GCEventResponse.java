package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.commons.enums.TypedEnum;
import com.gcplot.model.gc.GCEvent;
import com.google.common.base.Preconditions;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         9/12/16
 */
public class GCEventResponse {
    @JsonProperty("p")
    public long pauseMu;
    @JsonProperty("d")
    public long dateTime;
    @JsonProperty("g")
    public int[] generations;
    @JsonProperty("c")
    public int concurrency;
    @JsonProperty("cp")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public CapacityResponse capacity;
    @JsonProperty("tc")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public CapacityResponse totalCapacity;
    @JsonProperty("e")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String ext;

    public GCEventResponse(@JsonProperty("p") long pauseMu, @JsonProperty("d") long dateTime,
                           @JsonProperty("g") int[] generations, @JsonProperty("c") int concurrency,
                           @JsonProperty("cp")
                           @JsonInclude(JsonInclude.Include.NON_EMPTY) CapacityResponse capacity,
                           @JsonProperty("tc")
                           @JsonInclude(JsonInclude.Include.NON_EMPTY) CapacityResponse totalCapacity,
                           @JsonProperty("e")
                           @JsonInclude(JsonInclude.Include.NON_EMPTY) String ext) {
        this.pauseMu = pauseMu;
        this.dateTime = dateTime;
        this.generations = generations;
        this.concurrency = concurrency;
        this.capacity = capacity;
        this.totalCapacity = totalCapacity;
        this.ext = ext;
    }

    public static GCEventResponse from(GCEvent event, DateTimeZone tz) {
        Preconditions.checkNotNull(tz);
        if (event == null) {
            return null;
        }
        int[] gens = event.generations().stream().mapToInt(TypedEnum::type).toArray();
        return new GCEventResponse(event.pauseMu(), event.occurred().toDateTime(tz).getMillis(),
                gens, event.concurrency().type(), CapacityResponse.from(event.capacity()),
                CapacityResponse.from(event.totalCapacity()), event.ext());
    }

    public static String toJson(GCEvent event, DateTimeZone tz) {
        StringBuilder sb = new StringBuilder(128);
        int[] gens = event.generations().stream().mapToInt(TypedEnum::type).toArray();
        sb.append("{").append("\"p\":").append(event.pauseMu()).append(",")
                .append("\"d\":").append(event.occurred().toDateTime(tz).getMillis()).append(",")
                .append("\"g\":[");
        for (int i = 0; i < gens.length; i++) {
            sb.append(gens[i]);
            if (i < gens.length - 1) {
                sb.append(",");
            }
         }
        sb.append("],");
        sb.append("\"c\":").append(event.concurrency().type());
        if (event.capacity() != null) {
            sb.append(",\"cp\":").append(CapacityResponse.toJson(event.capacity()));
        }
        if (event.totalCapacity() != null) {
            sb.append(",\"tc\":").append(CapacityResponse.toJson(event.totalCapacity()));
        }
        if (event.ext() != null) {
            sb.append(",\"e\":").append("\"").append(event.ext()).append("\"");
        }
        return sb.append("}").toString();
    }

    public static List<GCEventResponse> from(List<GCEvent> events, DateTimeZone tz) {
        return events.stream().map(e -> from(e, tz)).collect(Collectors.toList());
    }
}
