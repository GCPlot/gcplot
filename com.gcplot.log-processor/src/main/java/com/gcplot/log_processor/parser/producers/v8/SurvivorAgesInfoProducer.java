package com.gcplot.log_processor.parser.producers.v8;

import com.gcplot.logs.survivor.AgesState;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/1/16
 */
public class SurvivorAgesInfoProducer {
    private static final String DESIRED_PREFIX = "Desired";
    private static final Pattern DESIRED_SIZE_PATTERN = Pattern.compile(DESIRED_PREFIX + " survivor size (?<sz>[0-9]+) bytes");
    private static final String AGE_NUM_GROUP = "agenum";
    private static final String AGE_OCCUPIED_GROUP = "occupied";
    private static final String AGE_TOTAL_GROUP = "total";
    private static final String AGES_PREFIX = "- age";
    private static final Pattern AGE_PATTERN = Pattern.compile(AGES_PREFIX + "[ \\t]+(?<" + AGE_NUM_GROUP + ">[0-9]+):[ \\t]+" +
            "(?<" + AGE_OCCUPIED_GROUP + ">[0-9]+)[ \\t]+bytes,[ \\t]+(?<" + AGE_TOTAL_GROUP + ">[0-9]+)[ \\t]+total");
    protected SortedMap<Integer, List<Pair<Long, Long>>> ages = new TreeMap<>();
    protected long desiredSurvivorSize;
    protected long desiredSurvivorCount;
    protected Runnable onFinished = () -> {};

    public void parse(String s) {
        if (s.startsWith(AGES_PREFIX)) {
            Matcher m = AGE_PATTERN.matcher(s);
            if (m.matches()) {
                int age = Integer.parseInt(m.group(AGE_NUM_GROUP));
                long ocp = Long.parseLong(m.group(AGE_OCCUPIED_GROUP));
                long ttl = Long.parseLong(m.group(AGE_TOTAL_GROUP));
                ages.computeIfAbsent(age, k -> new ArrayList<>()).add(Pair.of(ocp, ttl));
            }
        } else if (s.startsWith(DESIRED_PREFIX)) {
            Matcher m = DESIRED_SIZE_PATTERN.matcher(s);
            if (m.find()) {
                desiredSurvivorSize += Long.parseLong(m.group("sz"));
                desiredSurvivorCount++;
            }
        }
    }

    public void finish() {
        onFinished.run();
    }

    public AgesState averageAgesState() {
        long dss = desiredSurvivorSize / Math.max(desiredSurvivorCount, 1);
        if (ages.size() == 0) {
            return AgesState.NONE;
        }
        long[] occupied = new long[ages.size()];
        long[] total = new long[ages.size()];
        long[] count = new long[ages.size()];

        AtomicInteger ctr = new AtomicInteger();
        ages.forEach((age, sizes) -> {
            for (Pair<Long, Long> size : sizes) {
                occupied[ctr.get()] += size.getLeft();
                total[ctr.get()] += size.getRight();
                count[ctr.get()]++;
            }
            ctr.incrementAndGet();
        });
        for (int i = 0; i < ages.size(); i++) {
            occupied[i] /= Math.max(count[i], 1);
            total[i] /= Math.max(count[i], 1);
        }

        return new AgesState(dss, Arrays.asList(ArrayUtils.toObject(occupied)),
                Arrays.asList(ArrayUtils.toObject(total)));
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

}
