package com.gcplot.repository;

import com.gcplot.model.gc.*;
import com.gcplot.repository.cassandra.CassandraGCEventRepository;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class TestCassandraGCEventRepository extends BaseCassandraTest {

    @Test
    public void test() throws Exception {
        String analyseId = UUID.randomUUID().toString();
        String jvmId = "jvm1";
        CassandraGCEventRepository r = new CassandraGCEventRepository();
        r.setConnector(connector);
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

        Assert.assertNotNull(ge.id());
        // we don't load jvmId and analyseId in order to save traffic
        // Assert.assertEquals(ge.jvmId(), jvmId);
        // Assert.assertEquals(ge.analyseId(), analyseId);
        Assert.assertNull(ge.jvmId());
        Assert.assertNull(ge.analyseId());
        Assert.assertEquals(ge.description(), event.description());
        Assert.assertEquals(ge.occurred(), event.occurred().toDateTime(DateTimeZone.UTC));
        Assert.assertEquals(ge.vmEventType(), event.vmEventType());
        Assert.assertEquals(ge.capacity(), event.capacity());
        Assert.assertEquals(ge.totalCapacity(), new Capacity(event.totalCapacity()));
        Assert.assertEquals(ge.pauseMu(), event.pauseMu());
        Assert.assertEquals(ge.generations(), event.generations());
        Assert.assertEquals(ge.concurrency(), event.concurrency());

        ge = r.lazyPauseEvents(analyseId, jvmId, wideDays(2)).next();
        Assert.assertNull(ge.id());
        Assert.assertNull(ge.jvmId());
        Assert.assertNull(ge.analyseId());
        Assert.assertNull(ge.description());
        Assert.assertNotNull(ge.occurred());
        Assert.assertNotNull(ge.vmEventType());
        Assert.assertNull(ge.capacity());
        Assert.assertNull(ge.totalCapacity());
        Assert.assertNotEquals(ge.pauseMu(), 0);
        Assert.assertNotNull(ge.generations());
        Assert.assertNotNull(ge.concurrency());
    }

    @Test
    public void testExceedingFetchSize() throws Exception {
        String analyseId = UUID.randomUUID().toString();
        String jvmId = "jvm1";

        CassandraGCEventRepository r = new CassandraGCEventRepository();
        r.setConnector(connector);
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
                .capacity(Capacity.of(3000, 1000, 6000))
                .totalCapacity(Capacity.NONE)
                .pauseMu(51321)
                .generations(EnumSet.of(Generation.YOUNG, Generation.TENURED))
                .concurrency(EventConcurrency.SERIAL);
    }

}
