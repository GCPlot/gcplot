package com.gcplot.log_processor.parser.adapter;

import com.tagtraum.perf.gcviewer.imp.DataReaderSun1_6_0;
import com.tagtraum.perf.gcviewer.imp.GcLogType;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import org.junit.Test;

import java.io.InputStream;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/26/16
 */
public class TestGCViewerAPI {

    @Test
    public void test() throws Exception {
        InputStream log = getClass().getClassLoader().getResourceAsStream("gc_logs/cms_long_log_young_only.log");
        DataReaderSun1_6_0 r = new DataReaderSun1_6_0(new GCResource("res1"), log, GcLogType.SUN1_8);
        r.setExcludedHandler(s -> System.out.println("| " + s));
        r.setHeaderHandler(s -> System.out.println("+ " + s));
        GCModel model = r.read();
        System.out.println(model.getAllEvents().size());
        System.out.println(model);
    }

}
