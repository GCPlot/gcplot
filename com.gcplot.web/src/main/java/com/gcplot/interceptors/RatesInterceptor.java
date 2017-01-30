package com.gcplot.interceptors;

import com.gcplot.model.gc.GCEvent;
import com.gcplot.web.RequestContext;
import org.joda.time.DateTime;

import java.util.function.Consumer;

/**
 * Promotion and allocation rates interceptor which helps gather
 * time-based plot data. Depends on some time window.
 *
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         11/18/16
 */
public class RatesInterceptor extends BaseInterceptor implements Interceptor {
    private final int sampleSeconds;
    private DateTime edge = null;
    private DateTime edgeMinus = null;
    private StringBuilder sb = new StringBuilder(128);

    public RatesInterceptor(int sampleSeconds) {
        this.sampleSeconds = sampleSeconds;
    }

    @Override
    public void process(GCEvent event, Runnable delimit, RequestContext ctx) {
        if (edge == null) {
            edge(event);
        }
        if (event.isYoung()) {
            try {
                if (ratePreviousEvent != null) {
                    if (sampleSeconds > 1) {
                        if (edgeMinus.isBefore(event.occurred())) {
                            countRates(event);
                        } else {
                            flush(event, delimit, ctx);
                        }
                    } else {
                        countRates(event);
                        flush(event, delimit, ctx);
                    }
                }
            } finally {
                ratePreviousEvent = event;
            }
        }
    }

    protected void flush(GCEvent event, Runnable delimit, RequestContext ctx) {
        write(event, delimit, ctx);
        allocationRateSum = 0;
        allocationRateCount = 0;
        promotionRateSum = 0;
        promotionRateCount = 0;
        edge(event);
    }

    private void write(GCEvent event, Runnable delimit, RequestContext ctx) {
        try {
            if ((allocationRateCount > 0 && allocationRateSum > 0) || (promotionRateSum > 0 &&
                    promotionRateCount > 0)) {
                long allRate = allocationRateSum / Math.max(allocationRateCount, 1);
                long prRate = promotionRateSum / Math.max(promotionRateCount, 1);
                sb.append("{\"alr\":").append(allRate).append(",\"prr\":").append(prRate)
                        .append(",\"d\":").append(event.occurred().getMillis()).append("}");
                ctx.write(sb.toString());
                delimit.run();
            }
        } finally {
            sb.setLength(0);
        }
    }

    @Override
    public void complete(Runnable delimit, RequestContext ctx) {
    }

    protected void edge(GCEvent event) {
        edge = event.occurred();
        edgeMinus = edge.minusSeconds(sampleSeconds);
    }
}
