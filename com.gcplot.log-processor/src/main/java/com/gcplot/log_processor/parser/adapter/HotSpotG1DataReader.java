package com.gcplot.log_processor.parser.adapter;

import com.tagtraum.perf.gcviewer.imp.DataReaderSun1_6_0G1;
import com.tagtraum.perf.gcviewer.imp.GcLogType;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/7/16
 */
public class HotSpotG1DataReader extends DataReaderSun1_6_0G1 {
    private final Consumer<AbstractGCEvent<?>> eventConsumer;

    public HotSpotG1DataReader(Consumer<AbstractGCEvent<?>> eventConsumer,
                             GCResource gcResource, InputStream in, GcLogType gcLogType)
            throws UnsupportedEncodingException {
        super(gcResource, in, gcLogType);
        this.eventConsumer = eventConsumer;
    }

    @Override
    protected GCModel createGCModel() {
        StreamGCModel model = new StreamGCModel();
        model.setEventsConsumer(l -> l.forEach(eventConsumer));
        return model;
    }
}
