package com.gcplot.log_processor.parser.hotspot;

import com.gcplot.log_processor.parser.BaseLogsParser;
import com.gcplot.model.gc.GCAnalyse;
import com.gcplot.model.gc.GCEvent;

import java.io.InputStreamReader;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/18/16
 */
public class HotSpotG1LogsParser extends BaseLogsParser {

    @Override
    public long parse(InputStreamReader reader, GCAnalyse analyse, Consumer<GCEvent> eventsConsumer) {
        return 0;
    }

}
