package com.gcplot.repository;

import com.gcplot.model.gc.ObjectsAges;
import com.gcplot.model.gc.ObjectsAgesImpl;
import com.google.common.primitives.Longs;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/5/16
 */
public class TestCassandraObjectsAgesRepository extends BaseCassandraTest {

    @Test
    public void test() throws Exception {
        String analyseId = UUID.randomUUID().toString();
        String jvmId = "jvm1";
        CassandraObjectsAgesRepository r = new CassandraObjectsAgesRepository();
        r.setConnector(connector);
        r.init();

        Assert.assertEquals(0, r.events(analyseId, jvmId, wideDays(7)).size());
        r.erase(analyseId, jvmId, wideDays(7));

        ObjectsAgesImpl oa = new ObjectsAgesImpl();
        fillEvent(analyseId, jvmId, oa);
        r.add(oa);

        Assert.assertEquals(1, r.events(analyseId, jvmId, wideDays(7)).size());
        Iterator<ObjectsAges> i = r.lazyEvents(analyseId, jvmId, wideDays(7));
        Assert.assertTrue(i.hasNext());
        ObjectsAges event = i.next();
        Assert.assertNotNull(event);
        Assert.assertFalse(i.hasNext());

        Assert.assertNull(event.jvmId());
        Assert.assertNull(event.analyseId());
        Assert.assertEquals(event.ext(), oa.ext());
        Assert.assertEquals(event.occurred(), oa.occurred());
        Assert.assertArrayEquals(Longs.toArray(event.occupied()), Longs.toArray(oa.occupied()));
        Assert.assertArrayEquals(Longs.toArray(event.total()), Longs.toArray(oa.total()));
    }

    @Test
    public void testExceedingFetchSize() throws Exception {
        String analyseId = UUID.randomUUID().toString();
        String jvmId = "jvm1";

        CassandraObjectsAgesRepository r = new CassandraObjectsAgesRepository();
        r.setConnector(connector);
        r.setFetchSize(5);
        r.init();

        List<ObjectsAges> events = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            ObjectsAgesImpl event = new ObjectsAgesImpl();
            fillEvent(analyseId, jvmId, event, i + 1);
            events.add(event);
        }
        r.add(events);

        List<ObjectsAges> fetched = new ArrayList<>();
        Iterator<ObjectsAges> i = r.lazyEvents(analyseId, jvmId, wideDays(7));
        while (i.hasNext()) {
            fetched.add(i.next());
        }

        Assert.assertEquals(15, fetched.size());
        Assert.assertEquals(15, r.events(analyseId, jvmId, wideDays(7)).size());
        // because ORDER BY DESC
        Assert.assertEquals((long) fetched.get(0).occupied().get(0), 20L);
    }

    private void fillEvent(String analyseId, String jvmId, ObjectsAgesImpl oa) {
        fillEvent(analyseId, jvmId, oa, 0);
    }

    private void fillEvent(String analyseId, String jvmId, ObjectsAgesImpl oa, int addition) {
        oa.analyseId(analyseId).jvmId(jvmId)
                .occurred(DateTime.now(DateTimeZone.UTC).dayOfYear().roundCeilingCopy())
                .ext("ext1")
                .occupied(Arrays.asList(5L + addition, 6L + addition, 7L + addition))
                .total(Arrays.asList(10L + addition, 11L + addition, 12L + addition));
    }

}
