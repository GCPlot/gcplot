package com.gcplot.repository;

import com.gcplot.commons.Range;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class TestCassandraGCEventRepository extends BaseCassandraTest {

    @Test
    public void test() throws Exception {
        String analyseId = UUID.randomUUID().toString();
        CassandraGCEventRepository r = new CassandraGCEventRepository(connector);
        r.init();
        Assert.assertEquals(0, r.events(analyseId, "jvm1",
                wideDays(7)).size());
        r.erase(analyseId, "jvm1", wideDays(7));
    }

    protected Range wideDays(int days) {
        return Range.of(DateTime.now().minusDays(days), DateTime.now().plusDays(days));
    }

}
