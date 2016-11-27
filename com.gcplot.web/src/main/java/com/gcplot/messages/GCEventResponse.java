package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.commons.enums.TypedEnum;
import com.gcplot.model.gc.*;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         9/12/16
 */
@NotThreadSafe
public class GCEventResponse {
    @JsonProperty("p")
    public long pauseMu;
    @JsonProperty("d")
    public long dateTime;
    @JsonProperty("g")
    public int[] generations;
    @JsonProperty(value = "c", defaultValue = "2")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int concurrency;
    @JsonProperty(value = "ph", defaultValue = "0")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int phase;
    @JsonProperty("cp")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public CapacityResponse capacity;
    @JsonProperty("tc")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public CapacityResponse totalCapacity;
    @JsonProperty("ecp")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Map<Generation, Capacity> capacityByGeneration;
    @JsonProperty("e")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String ext;

    public GCEventResponse(@JsonProperty("p") long pauseMu, @JsonProperty("d") long dateTime,
                           @JsonProperty("g") int[] generations, @JsonProperty("c") int concurrency,
                           @JsonProperty("ph") @JsonInclude(JsonInclude.Include.NON_DEFAULT) int phase,
                           @JsonProperty("cp")
                           @JsonInclude(JsonInclude.Include.NON_EMPTY) CapacityResponse capacity,
                           @JsonProperty("tc")
                           @JsonInclude(JsonInclude.Include.NON_EMPTY) CapacityResponse totalCapacity,
                           @JsonProperty("ecp")
                           @JsonInclude(JsonInclude.Include.NON_EMPTY) Map<Generation, Capacity> capacityByGeneration,
                           @JsonProperty("e")
                           @JsonInclude(JsonInclude.Include.NON_EMPTY) String ext) {
        this.pauseMu = pauseMu;
        this.dateTime = dateTime;
        this.generations = generations;
        this.concurrency = concurrency;
        this.phase = phase;
        this.capacity = capacity;
        this.totalCapacity = totalCapacity;
        this.capacityByGeneration = capacityByGeneration;
        this.ext = ext;
    }

    public static GCEventResponse from(GCEvent event, DateTimeZone tz) {
        Preconditions.checkNotNull(tz);
        if (event == null) {
            return null;
        }
        int[] gens = event.generations().stream().mapToInt(TypedEnum::type).toArray();
        return new GCEventResponse(event.pauseMu(), event.occurred().toDateTime(tz).getMillis(),
                gens, event.concurrency().type(), event.phase().type(), CapacityResponse.from(event.capacity()),
                CapacityResponse.from(event.totalCapacity()), event.capacityByGeneration(), event.ext());
    }

    private static ThreadLocal<StringBuilder> stringBuilder = new ThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder initialValue() {
            return new StringBuilder(128);
        }
    };
    public static String toJson(GCEvent event) {
        StringBuilder sb = stringBuilder.get();
        try {
            int[] gens = event.generations().stream().mapToInt(TypedEnum::type).toArray();
            sb.append("{").append("\"p\":").append(event.pauseMu()).append(",")
                    .append("\"d\":").append(event.occurred().getMillis()).append(",")
                    .append("\"g\":[");
            for (int i = 0; i < gens.length; i++) {
                sb.append(gens[i]);
                if (i < gens.length - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
            if (event.phase() != Phase.OTHER) {
                sb.append(",").append("\"ph\":").append(event.phase().type());
            }
            if (event.concurrency() != EventConcurrency.SERIAL) {
                sb.append(",").append("\"c\":").append(event.concurrency().type());
            }
            if (event.capacity() != null && !event.capacity().equals(Capacity.NONE)) {
                sb.append(",\"cp\":").append(CapacityResponse.toJson(event.capacity()));
            }
            if (event.totalCapacity() != null && !event.totalCapacity().equals(Capacity.NONE)) {
                sb.append(",\"tc\":").append(CapacityResponse.toJson(event.totalCapacity()));
            }
            if (event.capacityByGeneration() != null && event.capacityByGeneration().size() > 0) {
                sb.append(",\"ecp\":{");
                int c = 0;
                for (Map.Entry<Generation, Capacity> ec : event.capacityByGeneration().entrySet()) {
                    sb.append("\"").append(ec.getKey().toString()).append("\":")
                            .append(CapacityResponse.toJson(ec.getValue()));
                    if (c < event.capacityByGeneration().size() - 1) {
                        sb.append(",");
                    }
                    c++;
                }
                sb.append("}");
            }
            if (!Strings.isNullOrEmpty(event.ext())) {
                sb.append(",\"e\":").append("\"").append(event.ext()).append("\"");
            }
            return sb.append("}").toString();
        } finally {
            sb.setLength(0);
        }
    }

    public static List<GCEventResponse> from(List<GCEvent> events, DateTimeZone tz) {
        return events.stream().map(e -> from(e, tz)).collect(Collectors.toList());
    }
}
