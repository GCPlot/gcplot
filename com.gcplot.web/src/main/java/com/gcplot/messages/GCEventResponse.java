package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.commons.enums.TypedEnum;
import com.gcplot.model.gc.GCEvent;
import com.google.common.base.Preconditions;
import org.joda.time.DateTimeZone;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         9/12/16
 */
public class GCEventResponse {
    @JsonProperty("ps")
    public long pauseMu;
    @JsonProperty("dt")
    public long dateTime;
    @JsonProperty("gn")
    public int[] generations;
    @JsonProperty("cnt")
    public int concurrency;
    @JsonProperty("cap")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public CapacityResponse capacity;
    @JsonProperty("tcap")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public CapacityResponse totalCapacity;
    @JsonProperty("ext")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String ext;

    public GCEventResponse(@JsonProperty("ps") long pauseMu, @JsonProperty("dt") long dateTime,
                           @JsonProperty("gn") int[] generations, @JsonProperty("cnt") int concurrency,
                           @JsonProperty("cap")
                           @JsonInclude(JsonInclude.Include.NON_EMPTY) CapacityResponse capacity,
                           @JsonProperty("tcap")
                           @JsonInclude(JsonInclude.Include.NON_EMPTY) CapacityResponse totalCapacity,
                           @JsonProperty("ext")
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

    public static List<GCEventResponse> from(List<GCEvent> events, DateTimeZone tz) {
        return events.stream().map(e -> from(e, tz)).collect(Collectors.toList());
    }
}
