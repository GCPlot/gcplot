package com.gcplot.repository;

import com.gcplot.Identifier;
import com.gcplot.model.VMVersion;
import com.gcplot.model.gc.*;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

public class TestCassandraGCAnalyseRepository extends BaseCassandraTest {

    @Test
    public void test() throws Exception {
        CassandraGCAnalyseRepository r = new CassandraGCAnalyseRepository();
        r.setConnector(connector);
        r.init();
        Assert.assertEquals(0, r.analyses().size());

        GCAnalyseImpl gcAnalyse = new GCAnalyseImpl();
        gcAnalyse.accountId(Identifier.fromStr("user1"))
                .isContinuous(true)
                .start(DateTime.now().minusDays(5))
                .lastEvent(DateTime.now().minusDays(1))
                .name("Analyse 1")
                .vmVersion(VMVersion.HOTSPOT_1_8)
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
        Assert.assertEquals(true, rawAnalyse.isContinuous());
        Assert.assertEquals(gcAnalyse.start().toDateTime(DateTimeZone.UTC), rawAnalyse.start());
        Assert.assertEquals(gcAnalyse.lastEvent().toDateTime(DateTimeZone.UTC), rawAnalyse.lastEvent());
        Assert.assertEquals(gcAnalyse.name(), rawAnalyse.name());
        Assert.assertEquals(gcAnalyse.vmVersion(), rawAnalyse.vmVersion());
        Assert.assertEquals(gcAnalyse.collectorType(), rawAnalyse.collectorType());
        Assert.assertEquals(0, Sets.difference(gcAnalyse.jvmIds(), rawAnalyse.jvmIds()).size());
        Assert.assertEquals("header1,header2", rawAnalyse.jvmHeaders().get("jvm1"));
        Assert.assertEquals("header3", rawAnalyse.jvmHeaders().get("jvm2"));
        Assert.assertEquals(gcAnalyse.jvmMemoryDetails().get("jvm1"), rawAnalyse.jvmMemoryDetails().get("jvm1"));
        Assert.assertEquals(gcAnalyse.jvmMemoryDetails().get("jvm2"), rawAnalyse.jvmMemoryDetails().get("jvm2"));

        Assert.assertTrue(r.analyse(rawAnalyse.id()).isPresent());
        Assert.assertEquals(1, r.analysesFor(Identifier.fromStr("user1")).size());

        DateTime newLastTime = DateTime.now(DateTimeZone.UTC).plusDays(1);
        r.updateLastEvent(rawAnalyse.accountId(), rawAnalyse.id(), newLastTime);
        rawAnalyse = r.analyse(rawAnalyse.id()).get();
        Assert.assertEquals(rawAnalyse.lastEvent(), newLastTime);

        MemoryDetails newMd = md(918, 3 * 1024, 2 * 1024, 17 * 1024, 9 * 1024);
        r.analyseJvm(rawAnalyse.accountId(), rawAnalyse.id(), "jvm3", "header4,header10", newMd);

        rawAnalyse = r.analyse(rawAnalyse.id()).get();
        Assert.assertEquals(3, rawAnalyse.jvmMemoryDetails().size());
        Assert.assertEquals(rawAnalyse.jvmMemoryDetails().get("jvm3"), newMd);
        Assert.assertEquals(rawAnalyse.jvmHeaders().get("jvm3"), "header4,header10");
        Assert.assertTrue(rawAnalyse.jvmIds().contains("jvm3"));

        r.removeAnalyse(rawAnalyse.accountId(), rawAnalyse.id());
        Assert.assertEquals(0, r.analyses().size());
    }

    private static MemoryDetails md(long pageSize, long physicalTotal, long physicalFree,
                                   long swapTotal, long swapFree) {
        return new MemoryDetailsImpl().pageSize(pageSize).physicalTotal(physicalTotal)
                .physicalFree(physicalFree).swapTotal(swapTotal).swapFree(swapFree);
    }

}
