package com.gcplot.log_processor.parser.adapter;

import com.tagtraum.perf.gcviewer.imp.DataReaderSun1_6_0G1;
import com.tagtraum.perf.gcviewer.imp.GcLogType;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/7/16
 */
public class HotSpotG1DataReader extends DataReaderSun1_6_0G1 implements StreamDataReader {
    private final Consumer<List<AbstractGCEvent<?>>> eventsConsumer;
    private final int batchSize;

    public HotSpotG1DataReader(Consumer<List<AbstractGCEvent<?>>> eventsConsumer, int batchSize,
                               GCResource gcResource, InputStream in, GcLogType gcLogType)
            throws UnsupportedEncodingException {
        super(gcResource, in, gcLogType);
        this.eventsConsumer = eventsConsumer;
        this.batchSize = batchSize;
    }

    @Override
    protected GCModel createGCModel() {
        StreamGCModel model = new StreamGCModel();
        model.setEventsConsumer(eventsConsumer);
        if (batchSize > 0) {
            model.setBatchSize(batchSize);
        }
        return model;
    }

    @Override
    public StreamGCModel readStream() throws IOException {
        return (StreamGCModel) read();
    }

    @Override
    public void excludedHandler(Consumer<String> c) {
        setExcludedHandler(c);
    }

    @Override
    public void headerHandler(Consumer<String> c) {
        setHeaderHandler(c);
    }
}
