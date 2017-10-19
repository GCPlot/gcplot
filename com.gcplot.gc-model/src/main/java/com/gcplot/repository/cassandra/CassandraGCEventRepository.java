package com.gcplot.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.gcplot.utils.Range;
import com.gcplot.utils.Utils;
import com.gcplot.utils.enums.EnumSetUtils;
import com.gcplot.model.gc.Capacity;
import com.gcplot.model.gc.GCEvent;
import com.gcplot.model.gc.Generation;
import com.gcplot.repository.GCEventRepository;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.cassandra.utils.UUIDGen;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Months;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static com.gcplot.utils.CollectionUtils.processKeyMap;
import static com.gcplot.model.gc.cassandra.Mapper.eventFrom;
import static com.gcplot.model.gc.cassandra.Mapper.lazyEventFrom;
import static com.gcplot.model.gc.cassandra.Mapper.eventsFrom;

public class CassandraGCEventRepository extends AbstractVMEventsCassandraRepository<GCEvent> implements GCEventRepository {
    private static final Logger LOG = LoggerFactory.getLogger(CassandraGCEventRepository.class);
    protected static final String TABLE_NAME = "gc_event";
    protected static final String BUCKET_TABLE_NAME = "gc_event_by_bucket";
    protected static final String DATE_PATTERN = "yyyy-MM";
    // See Mapper#lazyEventFrom in case of update
    public static final String[] NON_KEY_FIELDS = new String[] {
            "occurred", "pause_mu", "tmstm", "generations",
            "concurrency", "phase", "capacity", "total_capacity",
            "gen_cap_before", "gen_cap_after", "gen_cap_total", "cause",
            "properties", "user_time", "sys_time"};
    public static final String[] LAST_EVENT_FIELDS = Utils.concat(NON_KEY_FIELDS, new String[] { "bucket_id" });
    public static final String[] PAUSE_EVENT_FIELDS = new String[] { "occurred", "vm_event_type", "pause_mu", "tmstm",
            "phase", "generations", "concurrency" };

    @Override
    public Optional<GCEvent> lastEvent(String analyseId, String jvmId, String bucketId, DateTime start) {
        return singleEvent(analyseId, jvmId, bucketId, start, LAST_EVENT_FIELDS).flatMap(e -> Optional.of(eventFrom(e)));
    }

    @Override
    public Optional<GCEvent> lastEvent(String analyseId, String jvmId, DateTime start) {
        return singleEvent(analyseId, jvmId, null, start, NON_KEY_FIELDS).flatMap(e -> Optional.of(eventFrom(e)));
    }

    @Override
    public List<GCEvent> events(String analyseId, String jvmId, Range range) {
        return eventsFrom(events0(analyseId, jvmId, range, NON_KEY_FIELDS));
    }

    @Override
    public Iterator<GCEvent> lazyEvents(String analyseId, String jvmId, Range range) {
        final Iterator<Row> i = events0(analyseId, jvmId, range, NON_KEY_FIELDS);
        return new Iterator<GCEvent>() {
            @Override
            public boolean hasNext() {
                return i.hasNext();
            }

            @Override
            public GCEvent next() {
                return lazyEventFrom(i.next());
            }
        };
    }

    @Override
    public List<GCEvent> pauseEvents(String analyseId, String jvmId, Range range) {
        return eventsFrom(events0(analyseId, jvmId, range, PAUSE_EVENT_FIELDS));
    }

    @Override
    public Iterator<GCEvent> lazyPauseEvents(String analyseId, String jvmId, Range range) {
        final Iterator<Row> i = events0(analyseId, jvmId, range, PAUSE_EVENT_FIELDS);
        return new Iterator<GCEvent>() {
            @Override
            public boolean hasNext() {
                return i.hasNext();
            }

            @Override
            public GCEvent next() {
                return eventFrom(i.next());
            }
        };
    }

    @Override
    public void erase(String analyseId, String jvmId, Range range) {
        connector.session().execute(QueryBuilder.delete().all().from(TABLE_NAME)
                .where(eq("analyse_id", UUID.fromString(analyseId)))
                .and(eq("jvm_id", jvmId))
                .and(in("date", dates(range))));
    }

    @Override
    public void erase(String analyseId, List<String> jvmIds, Range range) {
        connector.session().execute(QueryBuilder.delete().all().from(TABLE_NAME)
                .where(eq("analyse_id", UUID.fromString(analyseId)))
                .and(in("jvm_id", jvmIds))
                .and(in("date", dates(range))));
    }

    protected Optional<Row> singleEvent(String analyseId, String jvmId, String bucketId,
                                        DateTime start, String[] fields) {
        List<String> dates = dates(Range.of(start.toDateTime(DateTimeZone.UTC),
                DateTime.now(DateTimeZone.UTC)));
        for (String date : dates) {
            Select from = QueryBuilder.select(fields).from(Strings.isNullOrEmpty(bucketId) ? TABLE_NAME : BUCKET_TABLE_NAME);
            Select.Where statement = from.limit(1)
                    .where(eq("analyse_id", UUID.fromString(analyseId)))
                    .and(eq("jvm_id", jvmId))
                    .and(eq("date", date));
            if (!Strings.isNullOrEmpty(bucketId)) {
                statement = statement.and(eq("bucket_id", bucketId));
            }
            List<Row> rows = connector.session().execute(statement).all();
            if (rows.size() > 0) {
                return Optional.of(rows.get(0));
            }
        }
        return Optional.empty();
    }

    private Iterator<Row> events0(String analyseId, String jvmId, Range range, String[] fields) {
        final Iterator<String> dates = dates(range).iterator();
        return new Iterator<Row>() {
            private Iterator<Row> resultIterator;

            @Override
            public boolean hasNext() {
                checkIterator();
                return resultIterator != null && resultIterator.hasNext();
            }

            @Override
            public Row next() {
                checkIterator();
                return resultIterator.next();
            }

            private void selectNext() {
                if (dates.hasNext()) {
                    String date = dates.next();
                    Statement statement = QueryBuilder.select(fields).from(TABLE_NAME)
                            .where(eq("analyse_id", UUID.fromString(analyseId)))
                            .and(eq("jvm_id", jvmId))
                            .and(eq("date", date))
                            .and(gte("written_at", QueryBuilder.fcall("minTimeuuid", range.from().getMillis())))
                            .and(lte("written_at", QueryBuilder.fcall("maxTimeuuid", range.to().getMillis()))).setFetchSize(fetchSize);
                    LOG.debug("Query: {}", statement);
                    resultIterator = connector.session().execute(statement).iterator();
                }
            }

            private void checkIterator() {
                while ((resultIterator == null || !resultIterator.hasNext()) && dates.hasNext()) {
                    selectNext();
                }
            }
        };
    }

    /**
     * Returns a range of dates in format YYYY-MM, starting from the most recent one.
     *
     * @param range
     * @return
     */
    protected List<String> dates(Range range) {
        return Lists.reverse(IntStream.range(0, Months.monthsBetween(
                range.from().monthOfYear().roundFloorCopy(),
                range.to().monthOfYear().roundCeilingCopy()).getMonths())
                .mapToObj(i -> range.from().plusMonths(i).toString(DATE_PATTERN))
                .collect(Collectors.toList()));
    }

    protected RegularStatement addStatement(GCEvent event) {
        return (RegularStatement) QueryBuilder.insertInto(TABLE_NAME).value("id", event.id() != null ? UUID.fromString(event.id()) : uuid())
                .value("parent_id", event.parentEvent().isPresent() ? UUID.fromString(event.parentEvent().orElse(null)) : null)
                .value("analyse_id", UUID.fromString(event.analyseId()))
                .value("bucket_id", event.bucketId())
                .value("date", event.occurred().toString(DATE_PATTERN))
                .value("jvm_id", event.jvmId())
                .value("description", event.description())
                .value("tmstm", event.timestamp())
                .value("written_at", UUIDGen.getTimeUUID(event.occurred().getMillis()))
                .value("occurred", event.occurred().getMillis())
                .value("cause", event.cause().type())
                .value("properties", event.properties())
                .value("vm_event_type", event.vmEventType().type())
                .value("capacity", Arrays.asList(event.capacity().usedBefore(), event.capacity().usedAfter(), event.capacity().total()))
                .value("total_capacity", Arrays.asList(event.totalCapacity().usedBefore(), event.totalCapacity().usedAfter(), event.totalCapacity().total()))
                .value("pause_mu", event.pauseMu())
                .value("user_time", event.user())
                .value("sys_time", event.sys())
                .value("real_time", event.real())
                .value("phase", event.phase().type())
                .value("generations", EnumSetUtils.encode(event.generations()))
                .value("concurrency", event.concurrency().type())
                .value("gen_cap_before", processKeyMap(event.capacityByGeneration(), Generation::type, Capacity::usedBefore))
                .value("gen_cap_after", processKeyMap(event.capacityByGeneration(), Generation::type, Capacity::usedAfter))
                .value("gen_cap_total", processKeyMap(event.capacityByGeneration(), Generation::type, Capacity::total))
                .value("ext", event.ext()).setConsistencyLevel(ConsistencyLevel.ONE);
    }

}
