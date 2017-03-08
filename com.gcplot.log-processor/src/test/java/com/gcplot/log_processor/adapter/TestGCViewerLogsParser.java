package com.gcplot.log_processor.adapter;

import com.gcplot.log_processor.common.TestGCEventFactory;
import com.gcplot.logs.ParseResult;
import com.gcplot.log_processor.parser.adapter.GCViewerLogsParser;
import com.gcplot.logs.ParserContext;
import com.gcplot.model.VMVersion;
import com.gcplot.model.gc.GCEvent;
import com.gcplot.model.gc.GarbageCollectorType;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/8/16
 */
public class TestGCViewerLogsParser {
    protected static final Logger LOG = LoggerFactory.getLogger(TestGCViewerLogsParser.class);

    @Test
    public void test() {
        InputStream log = getClass().getClassLoader().getResourceAsStream("gc_logs/cms_full_gc_log_2.log");
        List<GCEvent> events = new CopyOnWriteArrayList<>();
        GCViewerLogsParser p = new GCViewerLogsParser();
        p.init();
        p.setEventFactory(new TestGCEventFactory());
        GCEvent[] first = new GCEvent[1], last = new GCEvent[1];
        ParseResult pr = p.parse(log, e -> { first[0] = e; return true; }, e -> {
            if (e != null) {
                last[0] = e;
                events.add(e);
            }
        }, new ParserContext(LOG, "chcksm", GarbageCollectorType.ORACLE_CMS, VMVersion.HOTSPOT_1_8));
        p.destroy();

        Assert.assertEquals(first[0].timestamp(), 170240.954, 0.001);
        Assert.assertEquals(last[0].timestamp(), 170291.419, 0.001);
        Assert.assertEquals(1, events.stream().filter(GCEvent::isFull).count());
        GCEvent fullGcEvent = events.stream().filter(GCEvent::isFull).findFirst().get();
        Assert.assertEquals(5_072_694, fullGcEvent.pauseMu());
        // this metaspace event was part of the full gc event, so didn't counted here
        Assert.assertEquals(0, events.stream().filter(GCEvent::isMetaspace).count());
        Assert.assertEquals(0, events.stream().filter(GCEvent::isPerm).count());
        Assert.assertEquals("chcksm", events.get(0).bucketId());

        Assert.assertNotNull(pr);
        Assert.assertTrue(pr.isSuccessful());
        Assert.assertEquals(1, pr.getAgesStates().size());
        Assert.assertFalse(pr.getLogMetadata().isPresent());
    }

    @Test
    public void testStreaming() {
        InputStream log = getClass().getClassLoader().getResourceAsStream("gc_logs/cms_long_log_young_only.log");
        List<GCEvent> events = new CopyOnWriteArrayList<>();
        GCViewerLogsParser p = new GCViewerLogsParser();
        p.init();
        p.setEventFactory(new TestGCEventFactory());
        p.setBatchSize(64);
        GCEvent[] first = new GCEvent[1], last = new GCEvent[1];
        ParseResult pr = p.parse(log, e -> { first[0] = e; return true; }, e -> {
            if (e != null) {
                last[0] = e;
                events.add(e);
            }
        }, new ParserContext(LOG, "chcksm", GarbageCollectorType.ORACLE_CMS, VMVersion.HOTSPOT_1_8));
        p.destroy();

        Assert.assertEquals(first[0].timestamp(), 39996.730, 0.001);
        Assert.assertEquals(last[0].timestamp(), 40149.971, 0.001);
        Assert.assertNotNull(pr);
        Assert.assertEquals(0, events.stream().filter(GCEvent::isFull).count());
        Assert.assertEquals(81, events.size());
        for (int i = 2; i < events.size(); i++) {
            Assert.assertTrue("" + i, events.get(i).occurred().isAfter(events.get(i - 1).occurred()));
        }
    }

}
