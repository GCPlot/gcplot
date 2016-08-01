package com.gcplot.log_processor.parser.adapter;

import com.gcplot.log_processor.parser.LogsParser;
import com.gcplot.log_processor.parser.ParseResult;
import com.gcplot.log_processor.parser.SurvivorAgesInfoProducer;
import com.gcplot.model.gc.GCEvent;
import com.gcplot.model.gc.GarbageCollectorType;
import com.tagtraum.perf.gcviewer.imp.AbstractDataReaderSun;
import com.tagtraum.perf.gcviewer.imp.DataReaderSun1_6_0G1;
import com.tagtraum.perf.gcviewer.imp.GcLogType;
import com.tagtraum.perf.gcviewer.model.GCResource;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/24/16
 */
public class GCViewerLogsParser implements LogsParser {

    @Override
    public ParseResult parse(InputStream reader, Logger log, GarbageCollectorType type, Consumer<GCEvent> eventsConsumer) {
        SurvivorAgesInfoProducer agesInfoProducer = new SurvivorAgesInfoProducer();
        GCResource gcResource = new GCResource("default");
        gcResource.setLogger(log);
        AbstractDataReaderSun dr;
        try {
            if (type == GarbageCollectorType.ORACLE_G1) {
                dr = new DataReaderSun1_6_0G1(gcResource, reader, GcLogType.SUN1_8G1);
            } else {
                dr = new DataReaderSun1_6_0G1(gcResource, reader, GcLogType.SUN1_8);
            }
        } catch (UnsupportedEncodingException e) {
            return ParseResult.failure(e);
        }
        dr.setExcludedHandler(agesInfoProducer::parse);
        return null;
    }

}
