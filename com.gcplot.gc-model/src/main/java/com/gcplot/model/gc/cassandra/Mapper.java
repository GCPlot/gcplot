package com.gcplot.model.gc.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.gcplot.Identifier;
import com.gcplot.commons.enums.EnumSetUtils;
import com.gcplot.model.gc.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.*;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.ToLongFunction;

public abstract class Mapper {

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
                .lastEvent(new DateTime(row.getTimestamp("last_event"), DateTimeZone.UTC))
                .collectorType(GarbageCollectorType.get(row.getInt("gc_type")))
                .jvmIds(row.getSet("jvm_ids", String.class))
                .jvmHeaders(row.getMap("jvm_headers", String.class, String.class));
        Map<String, MemoryDetails> memoryDetails = new HashMap<>();
        Map<String, Long> jvmPageSizes = row.getMap("jvm_md_page_size", String.class, Long.class);
        Map<String, Long> jvmPhysTotal = row.getMap("jvm_md_phys_total", String.class, Long.class);
        Map<String, Long> jvmPhysFree = row.getMap("jvm_md_phys_free", String.class, Long.class);
        Map<String, Long> jvmSwapTotal = row.getMap("jvm_md_swap_total", String.class, Long.class);
        Map<String, Long> jvmSwapFree = row.getMap("jvm_md_swap_free", String.class, Long.class);
        for (String jvm : gcAnalyse.jvmIds()) {
            MemoryDetailsImpl details = new MemoryDetailsImpl();
            details.pageSize(jvmPageSizes.getOrDefault(jvm, 0L));
            details.physicalTotal(jvmPhysTotal.getOrDefault(jvm, 0L));
            details.physicalFree(jvmPhysFree.getOrDefault(jvm, 0L));
            details.swapTotal(jvmSwapTotal.getOrDefault(jvm, 0L));
            details.swapFree(jvmSwapFree.getOrDefault(jvm, 0L));
            memoryDetails.put(jvm, details);
        }
        return gcAnalyse.jvmMemoryDetails(memoryDetails);
    }

    public static List<GCEvent> eventsFrom(ResultSet resultSet) {
        List<GCEvent> events = new LinkedList<>();
        for (Row row : resultSet) {
            events.add(eventFrom(row));
        }
        return events;
    }

    public static GCEvent eventFrom(Row row) {
        if (row == null) {
            return null;
        }
        GCEventImpl gcEvent = new GCEventImpl();
        gcEvent.id(op(row, "id", r -> r.getUUID("id").toString()))
                .parentEvent(sop(row, "parent_id", r -> r.getUUID("parent_id")))
                .analyseId(sop(row, "analyse_id", r -> r.getUUID("analyse_id")))
                .jvmId(op(row, "jvm_id", r -> r.getString("jvm_id")))
                .description(op(row, "description", r -> r.getString("description")))
                .occurred(op(row, "occurred", r -> new DateTime(r.getTimestamp("occurred"), DateTimeZone.UTC)))
                .vmEventType(op(row, "vm_event_type", r -> VMEventType.get(r.getInt("vm_event_type"))))
                .capacity(op(row, "capacity", r -> new CapacityImpl(r.getList("capacity", Long.class))))
                .totalCapacity(op(row, "total_capacity", r -> new CapacityImpl(r.getList("total_capacity", Long.class))))
                .pauseMu(lop(row, "pause_mu", r -> r.getLong("pause_mu")))
                .durationMu(lop(row, "duration_mu", r -> r.getLong("duration_mu")))
                .generations(op(row, "generations", r -> EnumSetUtils.decode(r.getLong("generations"), Generation.class)))
                .concurrency(op(row, "concurrency", r -> EventConcurrency.get(r.getInt("concurrency"))))
                .ext(op(row, "ext", r -> r.getString("ext")));
        return gcEvent;
    }

    private static <T> T op(Row row, String name, Function<Row, T> f) {
        if (row.getColumnDefinitions().contains(name)) {
            return f.apply(row);
        } else {
            return null;
        }
    }

    private static long lop(Row row, String name, ToLongFunction<Row> f) {
        if (row.getColumnDefinitions().contains(name)) {
            return f.applyAsLong(row);
        } else {
            return 0;
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
