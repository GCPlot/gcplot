package com.gcplot.repository;

import com.gcplot.Identifier;
import com.gcplot.model.gc.*;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

public class TestCassandraGCAnalyseRepository extends BaseCassandraTest {

    @Test
    public void test() throws Exception {
        CassandraGCAnalyseRepository r = new CassandraGCAnalyseRepository(connector);
        r.init();
        Assert.assertEquals(0, r.analyses().size());

        GCAnalyseImpl gcAnalyse = new GCAnalyseImpl();
        gcAnalyse.accountId(Identifier.fromStr("user1"))
                .isContinuous(true)
                .start(DateTime.now().minusDays(5))
                .lastEvent(DateTime.now().minusDays(1))
                .name("Analyse 1")
                .collectorType(GarbageCollectorType.ORACLE_G1)
                .jvmIds(Sets.newHashSet("jvm1", "jvm2"))
                .jvmHeaders(map("jvm1", "header1,header2", "jvm2", "header3"))
                .jvmMemoryDetails(map("jvm1", md(1024, 8 * 1024, 4 * 1024, 1024, 812),
                        "jvm2", md(4 * 1024, 16 * 1024, 2 * 1024, 3 * 1024, 918)));
        r.newAnalyse(gcAnalyse);
        Assert.assertEquals(1, r.analyses().size());

        GCAnalyse rawAnalyse = r.analyses().get(0);
        Assert.assertNotNull(rawAnalyse.id());
        Assert.assertEquals(2, rawAnalyse.jvmMemoryDetails().size());
    }

    private static MemoryDetails md(long pageSize, long physicalTotal, long physicalFree,
                                   long swapTotal, long swapFree) {
        return new MemoryDetailsImpl().pageSize(pageSize).physicalTotal(physicalTotal)
                .physicalFree(physicalFree).swapTotal(swapTotal).swapFree(swapFree);
    }

}
