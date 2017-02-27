package com.gcplot.model.gc.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.gcplot.Identifier;
import com.gcplot.commons.enums.EnumSetUtils;
import com.gcplot.model.VMVersion;
import com.gcplot.model.gc.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

import static com.gcplot.commons.CollectionUtils.transformValue;

public abstract class Mapper {
    private static final Logger LOG = LoggerFactory.getLogger(Mapper.class);

    public static List<GCAnalyse> analysesFrom(ResultSet resultSet) {
        List<GCAnalyse> list = new LinkedList<>();
        for (Row aResultSet : resultSet) {
            list.add(analyseFrom(aResultSet));
        }
        return list;
    }

    public static GCAnalyse analyseFrom(Row row) {
        if (row == null) {
            return null;
        }
        GCAnalyseImpl gcAnalyse = new GCAnalyseImpl();
        gcAnalyse.id(row.getUUID("id").toString())
                .accountId(Identifier.fromStr(row.getString("account_id")))
                .name(row.getString("analyse_name"))
                .isContinuous(row.getBool("is_continuous"))
                .start(new DateTime(row.getTimestamp("start"), DateTimeZone.UTC))
                .firstEvent(transformValue(row.getMap("first_event", String.class, Date.class),
                        v -> new DateTime(v, DateTimeZone.UTC)))
                .lastEvent(transformValue(row.getMap("last_event", String.class, Date.class),
                        v -> new DateTime(v, DateTimeZone.UTC)))
                .jvmVersions(transformValue(row.getMap("jvm_versions", String.class, Integer.class), VMVersion::get))
                .jvmGCTypes(transformValue(row.getMap("jvm_gc_types", String.class, Integer.class), GarbageCollectorType::get))
                .jvmIds(row.getSet("jvm_ids", String.class))
                .timezone(op(row, "timezone", r -> r.getString("timezone")))
                .jvmNames(row.getMap("jvm_names", String.class, String.class))
                .jvmHeaders(row.getMap("jvm_headers", String.class, String.class))
                .ext(op(row, "ext", r -> r.getString("ext")));
        Map<String, MemoryDetails> memoryDetails = new HashMap<>();
        Map<String, Long> jvmPageSizes = row.getMap("jvm_md_page_size", String.class, Long.class);
        Map<String, Long> jvmPhysTotal = row.getMap("jvm_md_phys_total", String.class, Long.class);
        Map<String, Long> jvmPhysFree = row.getMap("jvm_md_phys_free", String.class, Long.class);
        Map<String, Long> jvmSwapTotal = row.getMap("jvm_md_swap_total", String.class, Long.class);
        Map<String, Long> jvmSwapFree = row.getMap("jvm_md_swap_free", String.class, Long.class);
        for (String jvm : gcAnalyse.jvmIds()) {
            MemoryDetails details = new MemoryDetails();
            details.pageSize(jvmPageSizes.getOrDefault(jvm, 0L))
                    .physicalTotal(jvmPhysTotal.getOrDefault(jvm, 0L))
                    .physicalFree(jvmPhysFree.getOrDefault(jvm, 0L))
                    .swapTotal(jvmSwapTotal.getOrDefault(jvm, 0L))
                    .swapFree(jvmSwapFree.getOrDefault(jvm, 0L));
            if (!details.isEmpty()) {
                memoryDetails.put(jvm, details);
            }
        }
        return gcAnalyse.jvmMemoryDetails(memoryDetails);
    }

    public static List<GCEvent> eventsFrom(Iterator<Row> resultSet) {
        List<GCEvent> events = new LinkedList<>();
        Row row;
        while (resultSet.hasNext()) {
            row = resultSet.next();
            GCEvent event = eventFrom(row);
            if (event != null) {
                events.add(event);
            }
        }
        return events;
    }

    public static GCEvent eventFrom(Row row) {
        if (row == null) {
            return null;
        }
        GCEventImpl gcEvent = new GCEventImpl();
        try {
            gcEvent.bucketId(op(row, "bucket_id", r -> r.getString("bucket_id")))
                    .timestamp(dop(row, "tmstm", r -> r.getDouble("tmstm")))
                    .occurred(op(row, "occurred", r -> new DateTime(r.getTimestamp("occurred"), DateTimeZone.UTC)))
                    .vmEventType(op(row, "vm_event_type", r -> VMEventType.get(r.getInt("vm_event_type"))))
                    .capacity(op(row, "capacity", r -> new Capacity(r.getList("capacity", Long.class))))
                    .totalCapacity(op(row, "total_capacity", r -> new Capacity(r.getList("total_capacity", Long.class))))
                    .pauseMu(lop(row, "pause_mu", r -> r.getLong("pause_mu")))
                    .phase(op(row, "phase", r -> Phase.get(r.getInt("phase"))))
                    .cause(dop(row, "cause", Cause.OTHER, r -> Cause.get(r.getInt("cause"))))
                    .properties(lop(row, "properties", r -> r.getLong("properties")))
                    .generations(op(row, "generations", r -> EnumSetUtils.decode(r.getLong("generations"), Generation.class)))
                    .concurrency(op(row, "concurrency", r -> EventConcurrency.get(r.getInt("concurrency"))));

            Map<Generation, Capacity> capacityByGeneration = Collections.emptyMap();
            if (gcEvent.generations().size() > 1 && row.getColumnDefinitions().contains("gen_cap_before") &&
                    row.getColumnDefinitions().contains("gen_cap_after") && row.getColumnDefinitions().contains("gen_cap_total")) {
                capacityByGeneration = new IdentityHashMap<>(2);
                Map<Integer, Long> before = row.getMap("gen_cap_before", Integer.class, Long.class);
                Map<Integer, Long> after = row.getMap("gen_cap_after", Integer.class, Long.class);
                Map<Integer, Long> total = row.getMap("gen_cap_total", Integer.class, Long.class);

                for (Generation g : gcEvent.generations()) {
                    capacityByGeneration.put(g, Capacity.of(
                            before.getOrDefault(g.type(), 0L),
                            after.getOrDefault(g.type(), 0L),
                            total.getOrDefault(g.type(), 0L)));
                }
            }

            gcEvent.capacityByGeneration(capacityByGeneration);
        } catch (Throwable t) {
            LOG.error(t.getMessage() + " | " + row.toString());
            return null;
        }
        return gcEvent;
    }

    public static List<ObjectsAges> objectsAgesFrom(ResultSet resultSet) {
        List<ObjectsAges> events = new LinkedList<>();
        for (Row row : resultSet) {
            events.add(objectsAgeFrom(row));
        }
        return events;
    }

    public static ObjectsAges objectsAgeFrom(Row r) {
        if (r == null) {
            return null;
        }
        ObjectsAgesImpl objectsAge = new ObjectsAgesImpl();
        objectsAge.analyseId(sop(r, "analyse_id", d -> d.getUUID("analyse_id")))
                .ext(op(r, "ext", d -> d.getString("ext")))
                .desiredSurvivorSize(lop(r, "desired_sv_size", d -> d.getLong("desired_sv_size")))
                .jvmId(op(r, "jvm_id", d -> d.getString("jvm_id")))
                .occupied(r.getList("occupied", Long.class))
                .total(r.getList("total", Long.class))
                .occurred(new DateTime(r.getTimestamp("occurred"), DateTimeZone.UTC));
        return objectsAge;
    }

    private static <T> T op(Row row, String name, Function<Row, T> f) {
        if (row.getColumnDefinitions().contains(name)) {
            return f.apply(row);
        } else {
            return null;
        }
    }

    private static <T> T dop(Row row, String name, T def, Function<Row, T> f) {
        if (row.getColumnDefinitions().contains(name)) {
            return f.apply(row);
        } else {
            return def;
        }
    }

    private static long lop(Row row, String name, ToLongFunction<Row> f) {
        if (row.getColumnDefinitions().contains(name)) {
            return f.applyAsLong(row);
        } else {
            return 0;
        }
    }

    private static double dop(Row row, String name, ToDoubleFunction<Row> f) {
        if (row.getColumnDefinitions().contains(name)) {
            return f.applyAsDouble(row);
        } else {
            return 0d;
        }
    }

    private static <T> String sop(Row row, String name, Function<Row, T> f) {
        if (row.getColumnDefinitions().contains(name)) {
            T t = f.apply(row);
            if (t != null) {
                return t.toString();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

}
