package com.gcplot.log_processor.adapter;

import com.gcplot.log_processor.LogMetadata;
import com.gcplot.log_processor.parser.producers.v8.MetadataInfoProducer;
import com.tagtraum.perf.gcviewer.imp.DataReaderSun1_6_0;
import com.tagtraum.perf.gcviewer.imp.GcLogType;
import com.tagtraum.perf.gcviewer.model.GCResource;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/6/16
 */
public class MetadataInfoProducerTest {

    @Test
    public void test() throws Exception {
        InputStream log = getClass().getClassLoader().getResourceAsStream("gc_logs/log_with_header.log");
        GCResource res = new GCResource("r");
        DataReaderSun1_6_0 r = new DataReaderSun1_6_0(res, log, GcLogType.SUN1_8);
        MetadataInfoProducer metadataInfoProducer = new MetadataInfoProducer();
        r.setHeaderHandler(metadataInfoProducer::parse);
        r.read();

        LogMetadata logMetadata = metadataInfoProducer.getLogMetadata();
        Assert.assertNotNull(logMetadata);
        Assert.assertNotNull(logMetadata.commandLines());
        Assert.assertEquals(logMetadata.pageSize(), 4 * 1024);
        Assert.assertEquals(logMetadata.physicalTotal(), 8_328_839_168L);
        Assert.assertEquals(logMetadata.physicalFree(), 6_713_634_816L);
        Assert.assertEquals(logMetadata.swapTotal(), 4_095_733_760L);
        Assert.assertEquals(logMetadata.swapFree(), 3_679_670_272L);
    }

}
