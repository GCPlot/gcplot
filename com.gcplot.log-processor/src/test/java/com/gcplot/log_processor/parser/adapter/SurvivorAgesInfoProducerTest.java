package com.gcplot.log_processor.parser.adapter;

import com.gcplot.log_processor.parser.survivor.SurvivorAgesInfoProducer;
import com.gcplot.log_processor.parser.survivor.AgesState;
import com.tagtraum.perf.gcviewer.imp.DataReaderSun1_6_0;
import com.tagtraum.perf.gcviewer.imp.GcLogType;
import com.tagtraum.perf.gcviewer.model.GCResource;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/1/16
 */
public class SurvivorAgesInfoProducerTest {

    @Test
    public void test() throws Exception {
        InputStream log = getClass().getClassLoader().getResourceAsStream("gc_logs/survivor_log_test.log");
        GCResource res = new GCResource("r");
        DataReaderSun1_6_0 r = new DataReaderSun1_6_0(res, log, GcLogType.SUN1_8);
        SurvivorAgesInfoProducer sp = new SurvivorAgesInfoProducer();
        r.setExcludedHandler(sp::parse);
        r.read();
        sp.finish();

        AgesState as = sp.averageAgesState();
        Assert.assertEquals(as.getAges(), 6);
        Assert.assertEquals(as.getOccupied().get(0), 20_037_749);
        Assert.assertEquals(as.getOccupied().get(5), 375_393);
    }

}
