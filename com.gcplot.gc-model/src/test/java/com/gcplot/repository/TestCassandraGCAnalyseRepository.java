package com.gcplot.repository;

import com.gcplot.Identifier;
import com.gcplot.model.VMVersion;
import com.gcplot.model.gc.*;
import com.gcplot.model.gc.analysis.GCAnalyse;
import com.gcplot.repository.cassandra.CassandraGCAnalyseRepository;
import com.gcplot.repository.operations.analyse.AddJvmOperation;
import com.gcplot.repository.operations.analyse.RemoveAnalyseOperation;
import com.gcplot.repository.operations.analyse.RemoveJvmOperation;
import com.gcplot.repository.operations.analyse.UpdateCornerEventsOperation;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.OptionalLong;
import java.util.UUID;

public class TestCassandraGCAnalyseRepository extends BaseCassandraTest {

    @Test
    public void test() throws Exception {
        CassandraGCAnalyseRepository r = new CassandraGCAnalyseRepository();
        r.setConnector(connector);
        r.init();
        Assert.assertEquals(0, r.analyses().size());

        GCAnalyseImpl gcAnalyse = new GCAnalyseImpl();
        Identifier accId = Identifier.fromStr("user1");
        gcAnalyse.accountId(accId)
                .isContinuous(true)
                .start(DateTime.now().minusDays(5))
                .lastEvent(Collections.singletonMap("jvm1", DateTime.now().minusDays(1)))
                .name("Analyse 1")
                .timezone("Africa/Harare")
                .jvmVersions(map("jvm1", VMVersion.HOTSPOT_1_7, "jvm2", VMVersion.HOTSPOT_1_8))
                .jvmGCTypes(map("jvm1", GarbageCollectorType.ORACLE_CMS, "jvm2", GarbageCollectorType.ORACLE_G1))
                .jvmIds(Sets.newHashSet("jvm1", "jvm2"))
                .jvmNames(map("jvm1", "JVM 1", "jvm2", "JVM 2"))
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
        Assert.assertEquals(gcAnalyse.lastEvent().get("jvm1").toDateTime(DateTimeZone.UTC), rawAnalyse.lastEvent().get("jvm1"));
        Assert.assertEquals(gcAnalyse.name(), rawAnalyse.name());
        Assert.assertEquals(gcAnalyse.timezone(), rawAnalyse.timezone());
        Assert.assertEquals(gcAnalyse.jvmVersions(), rawAnalyse.jvmVersions());
        Assert.assertEquals(gcAnalyse.jvmGCTypes(), rawAnalyse.jvmGCTypes());
        Assert.assertEquals(0, Sets.difference(gcAnalyse.jvmIds(), rawAnalyse.jvmIds()).size());
        Assert.assertEquals("header1,header2", rawAnalyse.jvmHeaders().get("jvm1"));
        Assert.assertEquals("header3", rawAnalyse.jvmHeaders().get("jvm2"));
        Assert.assertEquals(gcAnalyse.jvmNames(), rawAnalyse.jvmNames());
        Assert.assertEquals(gcAnalyse.jvmMemoryDetails().get("jvm1"), rawAnalyse.jvmMemoryDetails().get("jvm1"));
        Assert.assertEquals(gcAnalyse.jvmMemoryDetails().get("jvm2"), rawAnalyse.jvmMemoryDetails().get("jvm2"));

        Assert.assertTrue(r.analyse(accId, rawAnalyse.id()).isPresent());
        Assert.assertEquals(1, r.analysesFor(Identifier.fromStr("user1")).size());

        DateTime firstTime = DateTime.now(DateTimeZone.UTC).minusDays(1);
        DateTime newLastTime = DateTime.now(DateTimeZone.UTC).plusDays(1);
        r.perform(new UpdateCornerEventsOperation(rawAnalyse.accountId(), rawAnalyse.id(), "jvm1", firstTime,
                newLastTime));
        rawAnalyse = r.analyse(accId, rawAnalyse.id()).get();
        Assert.assertEquals(rawAnalyse.firstEvent().get("jvm1"), firstTime);
        Assert.assertEquals(rawAnalyse.lastEvent().get("jvm1"), newLastTime);

        DateTime newFirstTime = DateTime.now(DateTimeZone.UTC).minusDays(2);
        newLastTime = DateTime.now(DateTimeZone.UTC).plusDays(2);
        r.perform(new UpdateCornerEventsOperation(rawAnalyse.accountId(), rawAnalyse.id(), "jvm1", newFirstTime,
                newLastTime));
        rawAnalyse = r.analyse(accId, rawAnalyse.id()).get();
        Assert.assertEquals(rawAnalyse.firstEvent().get("jvm1"), firstTime);
        Assert.assertEquals(rawAnalyse.lastEvent().get("jvm1"), newLastTime);

        MemoryDetails newMd = md(918, 3 * 1024, 2 * 1024, 17 * 1024, 9 * 1024);
        String jvm3 = "9a57c7a8-7aea-4737-96ba-d50b0a157902";
        r.perform(new AddJvmOperation(rawAnalyse.accountId(), rawAnalyse.id(), UUID.fromString(jvm3), "JVM 3", VMVersion.HOTSPOT_1_8,
                GarbageCollectorType.ORACLE_G1, "header4,header10", newMd));

        rawAnalyse = r.analyse(accId, rawAnalyse.id()).get();
        Assert.assertEquals(3, rawAnalyse.jvmMemoryDetails().size());
        Assert.assertEquals(rawAnalyse.jvmMemoryDetails().get(jvm3), newMd);
        Assert.assertEquals(rawAnalyse.jvmHeaders().get(jvm3), "header4,header10");
        Assert.assertEquals(rawAnalyse.jvmVersions().get(jvm3), VMVersion.HOTSPOT_1_8);
        Assert.assertEquals(rawAnalyse.jvmGCTypes().get(jvm3), GarbageCollectorType.ORACLE_G1);
        Assert.assertTrue(rawAnalyse.jvmIds().contains(jvm3));
        Assert.assertEquals(rawAnalyse.jvmNames().get(jvm3), "JVM 3");

        r.perform(new RemoveJvmOperation(rawAnalyse.accountId(), rawAnalyse.id(), "jvm2"));

        rawAnalyse = r.analyse(accId, rawAnalyse.id()).get();
        Assert.assertEquals(2, rawAnalyse.jvmMemoryDetails().size());
        Assert.assertNull(rawAnalyse.jvmMemoryDetails().get("jvm2"));
        Assert.assertNull(rawAnalyse.jvmVersions().get("jvm2"));
        Assert.assertNull(rawAnalyse.jvmGCTypes().get("jvm2"));
        Assert.assertNull(rawAnalyse.jvmNames().get("jvm2"));
        Assert.assertFalse(rawAnalyse.jvmIds().contains("jvm2"));

        OptionalLong count = r.analysesCount(gcAnalyse.accountId());
        Assert.assertEquals(1, count.getAsLong());

        r.perform(new RemoveAnalyseOperation(rawAnalyse.accountId(), rawAnalyse.id()));
        Assert.assertEquals(0, r.analyses().size());
        Assert.assertEquals(0, r.analysesCount(gcAnalyse.accountId()).getAsLong());
    }

    private static MemoryDetails md(long pageSize, long physicalTotal, long physicalFree,
                                   long swapTotal, long swapFree) {
        return new MemoryDetails().pageSize(pageSize).physicalTotal(physicalTotal)
                .physicalFree(physicalFree).swapTotal(swapTotal).swapFree(swapFree);
    }

}
