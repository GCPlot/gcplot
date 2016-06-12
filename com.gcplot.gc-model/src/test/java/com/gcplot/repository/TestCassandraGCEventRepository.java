package com.gcplot.repository;

import com.gcplot.commons.Range;
import com.gcplot.model.gc.*;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class TestCassandraGCEventRepository extends BaseCassandraTest {

    @Test
    public void test() throws Exception {
        String analyseId = UUID.randomUUID().toString();
        String jvmId = "jvm1";
        CassandraGCEventRepository r = new CassandraGCEventRepository(connector);
        r.init();
        Assert.assertEquals(0, r.events(analyseId, jvmId,
                wideDays(7)).size());
        r.erase(analyseId, jvmId, wideDays(7));

        GCEventImpl event = new GCEventImpl();
        fillEvent(analyseId, jvmId, event);
        r.add(event);

        Assert.assertEquals(1, r.events(analyseId, jvmId,
                wideDays(7)).size());
        Iterator<GCEvent> i = r.lazyEvents(analyseId, jvmId, wideDays(7));
        Assert.assertTrue(i.hasNext());
        GCEvent ge = i.next();
        Assert.assertNotNull(ge);
        Assert.assertFalse(i.hasNext());
    }

    @Test
    public void testExceedingFetchSize() throws Exception {
        String analyseId = UUID.randomUUID().toString();
        String jvmId = "jvm1";

        CassandraGCEventRepository r = new CassandraGCEventRepository(connector);
        r.setFetchSize(5);
        r.init();

        List<GCEvent> events = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            GCEventImpl event = new GCEventImpl();
            fillEvent(analyseId, jvmId, event);
            events.add(event);
        }
        r.add(events);

        events = new ArrayList<>();
        Iterator<GCEvent> i = r.lazyEvents(analyseId, jvmId, wideDays(7));
        while (i.hasNext()) {
            events.add(i.next());
        }

        Assert.assertEquals(15, events.size());
        Assert.assertEquals(15, r.events(analyseId, jvmId, wideDays(7)).size());
    }

    protected void fillEvent(String analyseId, String jvmId, GCEventImpl event) {
        event.jvmId(jvmId).analyseId(analyseId).description("descr1")
                .occurred(DateTime.now().minusDays(1))
                .vmEventType(VMEventType.GARBAGE_COLLECTION)
                .capacity(CapacityImpl.of(3000, 1000, 6000))
                .totalCapacity(Capacity.NONE)
                .pauseMu(51321)
                .durationMu(63124)
                .generations(EnumSet.of(Generation.YOUNG, Generation.TENURED))
                .concurrency(EventConcurrency.SERIAL);
    }

    protected Range wideDays(int days) {
        return Range.of(DateTime.now().minusDays(days), DateTime.now().plusDays(days));
    }

}
