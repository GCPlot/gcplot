package com.gcplot.services.stats;

import com.gcplot.model.GCRateImpl;
import com.gcplot.model.gc.GCEvent;
import com.gcplot.model.gc.GCRate;
import com.gcplot.commons.interceptors.EventInterceptor;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

/**
 * Promotion and allocation rates interceptor which helps gather
 * time-based plot data. Depends on some time window.
 *
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         11/18/16
 */
public class RatesInterceptor extends BaseInterceptor implements EventInterceptor<GCRate> {
    private final int sampleSeconds;
    private DateTime edge = null;
    private DateTime edgeMinus = null;

    public RatesInterceptor(int sampleSeconds) {
        this.sampleSeconds = sampleSeconds;
    }

    @Override
    public List<GCRate> process(GCEvent event) {
        if (edge == null) {
            edge(event);
        }
        if (event.isYoung() || (event.isFull() && event.hasYoungCapacity())) {
            try {
                if (ratePreviousEvent != null) {
                    if (sampleSeconds > 1) {
                        if (edgeMinus.isBefore(event.occurred())) {
                            countRates(event);
                        } else {
                            return flush(event);
                        }
                    } else {
                        countRates(event);
                        return flush(event);
                    }
                }
            } finally {
                ratePreviousEvent = event;
            }
        }
        return Collections.emptyList();
    }

    protected List<GCRate> flush(GCEvent event) {
        List<GCRate> or = write(event);
        allocationRateSum = 0;
        allocationRateCount = 0;
        promotionRateSum = 0;
        promotionRateCount = 0;
        edge(event);
        return or;
    }

    private List<GCRate> write(GCEvent event) {
        if ((allocationRateCount > 0 && allocationRateSum > 0) || (promotionRateSum > 0 &&
                promotionRateCount > 0)) {
            long allRate = allocationRateSum / Math.max(allocationRateCount, 1);
            long prRate = promotionRateSum / Math.max(promotionRateCount, 1);
            return Collections.singletonList(new GCRateImpl(event.occurred(), allRate, prRate));
        }
        return Collections.emptyList();
    }

    @Override
    public List<GCRate> complete() {
        return Collections.emptyList();
    }

    protected void edge(GCEvent event) {
        edge = event.occurred();
        edgeMinus = edge.minusSeconds(sampleSeconds);
    }
}
