package com.gcplot.repository;

import com.gcplot.model.gc.*;
import com.gcplot.repository.cassandra.CassandraGCEventRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class TestCassandraGCEventRepository extends BaseCassandraTest {
    public static final double TIMESTAMP_START = 123.456;

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
        fillEvent(analyseId, jvmId, bucketId(analyseId + jvmId), event);
        r.add(event);

        Assert.assertEquals(1, r.events(analyseId, jvmId,
                wideDays(7)).size());
        Iterator<GCEvent> i = r.lazyEvents(analyseId, jvmId, wideDays(7));
        Assert.assertTrue(i.hasNext());
        GCEvent ge = i.next();
        Assert.assertNotNull(ge);
        Assert.assertFalse(i.hasNext());

        Assert.assertNull(ge.id());
        // we don't load jvmId and analyseId in order to save traffic
        // Assert.assertEquals(ge.jvmId(), jvmId);
        // Assert.assertEquals(ge.analyseId(), analyseId);
        Assert.assertNull(ge.jvmId());
        Assert.assertNull(ge.analyseId());
        Assert.assertNull(ge.bucketId());
        Assert.assertNull(ge.description());
        Assert.assertEquals(ge.occurred(), event.occurred().toDateTime(DateTimeZone.UTC));
        Assert.assertEquals(ge.vmEventType(), event.vmEventType());
        Assert.assertEquals(ge.capacity(), event.capacity());
        Assert.assertEquals(ge.timestamp(), event.timestamp(), 0.0001);
        Assert.assertEquals(ge.totalCapacity(), new Capacity(event.totalCapacity()));
        Assert.assertEquals(ge.pauseMu(), event.pauseMu());
        Assert.assertEquals(ge.generations(), event.generations());
        Assert.assertEquals(ge.concurrency(), event.concurrency());

        GCEvent pauseGe = r.lazyPauseEvents(analyseId, jvmId, wideDays(2)).next();
        Assert.assertNull(pauseGe.id());
        Assert.assertNull(pauseGe.jvmId());
        Assert.assertNull(pauseGe.bucketId());
        Assert.assertNull(pauseGe.analyseId());
        Assert.assertNull(pauseGe.description());
        Assert.assertNotNull(pauseGe.occurred());
        Assert.assertNotNull(pauseGe.vmEventType());
        Assert.assertNull(pauseGe.capacity());
        Assert.assertNull(pauseGe.totalCapacity());
        Assert.assertNotEquals(pauseGe.pauseMu(), 0);
        Assert.assertNotNull(pauseGe.generations());
        Assert.assertNotNull(pauseGe.concurrency());

        Optional<GCEvent> oe = r.lastEvent(analyseId, jvmId, DateTime.now(DateTimeZone.UTC).minusDays(1));
        Assert.assertTrue(oe.isPresent());
        Assert.assertEquals(ge, oe.get());

        oe = r.lastEvent(analyseId, jvmId, bucketId(analyseId + jvmId), DateTime.now(DateTimeZone.UTC).minusDays(1));
        Assert.assertTrue(oe.isPresent());
        Assert.assertEquals(oe.get().bucketId(), bucketId(analyseId + jvmId));
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
        for (int i = 0; i < 30; i++) {
            GCEventImpl event = new GCEventImpl();
            fillEvent(analyseId, jvmId,
                    i < 15 ? bucketId(analyseId + jvmId) : bucketId(jvmId + analyseId), i + 1, event);
            events.add(event);
        }
        r.add(events);

        events = new ArrayList<>();
        Iterator<GCEvent> i = r.lazyEvents(analyseId, jvmId, wideDays(7));
        while (i.hasNext()) {
            events.add(i.next());
        }

        Assert.assertEquals(30, events.size());
        Assert.assertEquals(30, r.events(analyseId, jvmId, wideDays(7)).size());

        DateTime start = DateTime.now(DateTimeZone.UTC).minusDays(1);
        Optional<GCEvent> oe = r.lastEvent(analyseId, jvmId, start);
        Assert.assertTrue(oe.isPresent());
        Assert.assertEquals(events.get(0), oe.get());

        Optional<GCEvent> firstHalfBucket =
                r.lastEvent(analyseId, jvmId, bucketId(analyseId + jvmId), start);
        Optional<GCEvent> lastHalfBucket =
                r.lastEvent(analyseId, jvmId, bucketId(jvmId + analyseId), start);
        Assert.assertTrue(firstHalfBucket.isPresent());
        Assert.assertTrue(lastHalfBucket.isPresent());
        Assert.assertNotEquals(firstHalfBucket.get(), lastHalfBucket.get());
        Assert.assertEquals(TIMESTAMP_START + 15, firstHalfBucket.get().timestamp(), 0.001);
        Assert.assertEquals(TIMESTAMP_START + 30, lastHalfBucket.get().timestamp(), 0.001);
    }

    protected void fillEvent(String analyseId, String jvmId, String bucketId, GCEventImpl event) {
        fillEvent(analyseId, jvmId, bucketId, 0, event);
    }

    protected void fillEvent(String analyseId, String jvmId, String bucketId,
                             double seconds, GCEventImpl event) {
        event.jvmId(jvmId).analyseId(analyseId).description("descr1")
                .bucketId(bucketId)
                .occurred(DateTime.now(DateTimeZone.UTC).minusDays(1).plusSeconds((int) seconds))
                .vmEventType(VMEventType.GARBAGE_COLLECTION)
                .timestamp(TIMESTAMP_START + seconds)
                .capacity(Capacity.of(3000, 1000, 6000))
                .totalCapacity(Capacity.NONE)
                .pauseMu(51321)
                .generations(EnumSet.of(Generation.YOUNG, Generation.TENURED))
                .concurrency(EventConcurrency.SERIAL);
    }

    protected String bucketId(String salt) {
        return DigestUtils.md5Hex(salt);
    }

}
