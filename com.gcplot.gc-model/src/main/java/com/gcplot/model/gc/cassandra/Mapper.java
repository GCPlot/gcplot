package com.gcplot.model.gc.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.gcplot.Identifier;
import com.gcplot.model.gc.*;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
                .start(new DateTime(row.getTimestamp("start")))
                .lastEvent(new DateTime(row.getTimestamp("last_event")))
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

}
