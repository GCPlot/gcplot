package com.gcplot.log_processor.parser.adapter;

import com.gcplot.configuration.ConfigurationManager;
import com.gcplot.log_processor.parser.LogsParser;
import com.gcplot.log_processor.parser.ParseResult;
import com.gcplot.log_processor.parser.ParserContext;
import com.gcplot.log_processor.parser.producers.v8.MetadataInfoProducer;
import com.gcplot.log_processor.parser.producers.v8.SurvivorAgesInfoProducer;
import com.gcplot.model.gc.GCEvent;
import com.gcplot.model.gc.GarbageCollectorType;
import com.tagtraum.perf.gcviewer.imp.AbstractDataReaderSun;
import com.tagtraum.perf.gcviewer.imp.DataReaderSun1_6_0G1;
import com.tagtraum.perf.gcviewer.imp.GcLogType;
import com.tagtraum.perf.gcviewer.model.GCResource;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/24/16
 */
public class GCViewerLogsParser implements LogsParser {
    protected ConfigurationManager configurationManager;

    @Override
    public ParseResult parse(InputStream reader, Consumer<GCEvent> eventsConsumer, ParserContext pc) {
        SurvivorAgesInfoProducer agesInfoProducer = new SurvivorAgesInfoProducer();
        MetadataInfoProducer metadataInfoProducer = new MetadataInfoProducer();
        GCResource gcResource = new GCResource("default");
        gcResource.setLogger(pc.logger());
        AbstractDataReaderSun dr;
        try {
            if (pc.collectorType() == GarbageCollectorType.ORACLE_G1) {
                dr = new DataReaderSun1_6_0G1(gcResource, reader, GcLogType.SUN1_8G1);
            } else {
                dr = new DataReaderSun1_6_0G1(gcResource, reader, GcLogType.SUN1_8);
            }
        } catch (UnsupportedEncodingException e) {
            return ParseResult.failure(e);
        }
        dr.setExcludedHandler(agesInfoProducer::parse);
        dr.setHeaderHandler(metadataInfoProducer::parse);

        return null;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }
}
