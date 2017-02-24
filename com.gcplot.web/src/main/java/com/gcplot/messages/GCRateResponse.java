package com.gcplot.messages;

import com.gcplot.model.gc.GCRate;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         2/24/17
 */
public class GCRateResponse {
    private static ThreadLocal<StringBuilder> stringBuilder = ThreadLocal.withInitial(
            () -> new StringBuilder(128));

    public static String toJson(GCRate rate) {
        StringBuilder sb = stringBuilder.get();
        try {
            sb.append("{\"alr\":").append(rate.allocationRate()).append(",\"prr\":").append(rate.promotionRate())
                    .append(",\"d\":").append(rate.occurred().getMillis()).append("}");
            return sb.toString();
        } finally {
            sb.setLength(0);
        }
    }

}
