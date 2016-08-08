package com.gcplot.log_processor.adapter;

import com.gcplot.log_processor.common.TestGCEventFactory;
import com.gcplot.log_processor.parser.ParserContext;
import com.gcplot.log_processor.parser.adapter.GCViewerLogsParser;
import com.gcplot.model.gc.GCEvent;
import com.gcplot.model.gc.GarbageCollectorType;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/8/16
 */
public class TestGCViewerLogsParser {
    protected static final Logger LOG = LoggerFactory.getLogger(TestGCViewerLogsParser.class);

    @Test
    public void test() {
        InputStream log = getClass().getClassLoader().getResourceAsStream("gc_logs/cms_full_gc_log_2.log");
        List<GCEvent> events = new ArrayList<>();
        GCViewerLogsParser p = new GCViewerLogsParser();
        p.setEventFactory(new TestGCEventFactory());
        p.parse(log, events::add, new ParserContext(LOG, GarbageCollectorType.ORACLE_CMS));

        Assert.assertEquals(events.stream().filter(GCEvent::isFull).count(), 1);
        GCEvent fullGcEvent = events.stream().filter(GCEvent::isFull).findFirst().get();
        Assert.assertEquals(fullGcEvent.pauseMu(), 5_072_694);
        Assert.assertEquals(events.stream().filter(GCEvent::isMetaspace).count(), 1);
        Assert.assertEquals(events.stream().filter(GCEvent::isPerm).count(), 0);
    }

}
