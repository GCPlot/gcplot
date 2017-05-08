package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.commons.CollectionUtils;
import com.gcplot.commons.enums.TypedEnum;
import com.gcplot.model.gc.*;
import com.google.common.base.Strings;
import com.google.common.math.DoubleMath;

import javax.annotation.concurrent.NotThreadSafe;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         9/12/16
 */
@NotThreadSafe
public class GCEventResponse {
    private static final Double TOLERANCE = Math.pow(10, -6);
    private static final DecimalFormat FORMAT;

    static {
        DecimalFormat df = new DecimalFormat("#.######");
        df.setMaximumFractionDigits(6);
        FORMAT = df;
    }

    @JsonProperty("p")
    public long pauseMu;
    @JsonProperty("d")
    public long dateTime;
    @JsonProperty(value = "g", defaultValue = "1")
    public int[] generations;
    @JsonProperty(value = "c", defaultValue = "2")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int concurrency;
    @JsonProperty(value = "ph", defaultValue = "0")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int phase;
    @JsonProperty(value = "cs", defaultValue = "0")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int cause;
    @JsonProperty(value = "pp", defaultValue = "0")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public long properties;
    @JsonProperty(value = "u", defaultValue = "0")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public double user;
    @JsonProperty(value = "s", defaultValue = "0")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public double sys;
    @JsonProperty(value = "r", defaultValue = "0")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public double real;
    @JsonProperty("cp")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public CapacityResponse capacity;
    @JsonProperty("tc")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public CapacityResponse totalCapacity;
    @JsonProperty("ecp")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Map<String, CapacityResponse> capacityByGeneration;
    @JsonProperty("e")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String ext;

    public GCEventResponse(@JsonProperty("p") long pauseMu, @JsonProperty("d") long dateTime,
                           @JsonProperty("g") int[] generations, @JsonProperty("c") int concurrency,
                           @JsonProperty("ph") @JsonInclude(JsonInclude.Include.NON_DEFAULT) int phase,
                           @JsonProperty(value = "cs", defaultValue = "0") @JsonInclude(JsonInclude.Include.NON_DEFAULT) int cause,
                           @JsonProperty(value = "pp", defaultValue = "0") @JsonInclude(JsonInclude.Include.NON_DEFAULT) long properties,
                           @JsonProperty(value = "u", defaultValue = "0")
                           @JsonInclude(JsonInclude.Include.NON_DEFAULT) double user,
                           @JsonProperty(value = "s", defaultValue = "0")
                           @JsonInclude(JsonInclude.Include.NON_DEFAULT) double sys,
                           @JsonProperty(value = "r", defaultValue = "0")
                           @JsonInclude(JsonInclude.Include.NON_DEFAULT) double real,
                           @JsonProperty("cp")
                           @JsonInclude(JsonInclude.Include.NON_EMPTY) CapacityResponse capacity,
                           @JsonProperty("tc")
                           @JsonInclude(JsonInclude.Include.NON_EMPTY) CapacityResponse totalCapacity,
                           @JsonProperty("ecp")
                           @JsonInclude(JsonInclude.Include.NON_EMPTY) Map<String, CapacityResponse> capacityByGeneration,
                           @JsonProperty("e")
                           @JsonInclude(JsonInclude.Include.NON_EMPTY) String ext) {
        this.pauseMu = pauseMu;
        this.dateTime = dateTime;
        this.generations = generations;
        this.concurrency = concurrency;
        this.phase = phase;
        this.cause = cause;
        this.properties = properties;
        this.user = user;
        this.sys = sys;
        this.real = real;
        this.capacity = capacity;
        this.totalCapacity = totalCapacity;
        this.capacityByGeneration = capacityByGeneration;
        this.ext = ext;
    }

    public static GCEventResponse from(GCEvent event) {
        if (event == null) {
            return null;
        }
        int[] gens = event.generations().stream().mapToInt(TypedEnum::type).toArray();
        Map<String, CapacityResponse> cbg = event.capacityByGeneration().size() != 0 ?
                CollectionUtils.processKeyMap(event.capacityByGeneration(), Generation::toString, CapacityResponse::from) :
                Collections.emptyMap();
        return new GCEventResponse(event.pauseMu(), event.occurred().getMillis(),
                gens, event.concurrency().type(), event.phase().type(), event.cause().type(),
                event.properties(), event.user(), event.sys(), event.real(), CapacityResponse.from(event.capacity()),
                CapacityResponse.from(event.totalCapacity()), cbg, event.ext());
    }

    private static ThreadLocal<StringBuilder> stringBuilder = ThreadLocal.withInitial(
            () -> new StringBuilder(256));

    public static String toJson(GCEvent event) {
        StringBuilder sb = stringBuilder.get();
        try {
            int[] gens = event.generations().stream().mapToInt(TypedEnum::type).toArray();
            sb.append("{").append("\"p\":").append(event.pauseMu()).append(",")
                    .append("\"d\":").append(event.occurred().getMillis());
            if (gens.length != 1 || gens[0] != Generation.YOUNG.type()) {
                sb.append(",").append("\"g\":[");
                for (int i = 0; i < gens.length; i++) {
                    sb.append(gens[i]);
                    if (i < gens.length - 1) {
                        sb.append(",");
                    }
                }
                sb.append("]");
            }
            if (event.phase() != Phase.OTHER) {
                sb.append(",").append("\"ph\":").append(event.phase().type());
            }
            if (event.concurrency() != EventConcurrency.SERIAL) {
                sb.append(",").append("\"c\":").append(event.concurrency().type());
            }
            if (event.properties() != 0) {
                sb.append(",").append("\"pp\":").append(event.properties());
            }
            if (event.cause() != Cause.OTHER) {
                sb.append(",").append("\"cs\":").append(event.cause().type());
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
            if (event.user() >= 0.0) {
                sb.append(",\"u\":").append(FORMAT.format(event.user()));
            }
            if (event.sys() >= 0.0) {
                sb.append(",\"s\":").append(FORMAT.format(event.sys()));
            }
            if (!Strings.isNullOrEmpty(event.ext())) {
                sb.append(",\"e\":").append("\"").append(event.ext()).append("\"");
            }
            return sb.append("}").toString();
        } finally {
            sb.setLength(0);
        }
    }

    public static List<GCEventResponse> from(List<GCEvent> events) {
        return events.stream().map(GCEventResponse::from).collect(Collectors.toList());
    }
}
