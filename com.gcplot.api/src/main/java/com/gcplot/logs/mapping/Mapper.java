package com.gcplot.logs.mapping;

import com.gcplot.logs.ParserContext;
import com.gcplot.model.gc.GCEvent;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         6/14/17
 */
public interface Mapper<T> {

    Mapper EMPTY = (noDatestampOffset, ctx, rawEvent) -> null;

    GCEvent map(DateTime noDatestampOffset, ParserContext ctx, T rawEvent);

    DateTime START = DateTime.now(DateTimeZone.UTC).withDayOfYear(1).withTimeAtStartOfDay();

    default GCEvent map(ParserContext ctx, T rawEvent) {
        return map(START, ctx, rawEvent);
    }

}
