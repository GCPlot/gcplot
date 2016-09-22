package com.gcplot.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.gcplot.commons.Range;
import com.gcplot.commons.Utils;
import com.gcplot.commons.enums.EnumSetUtils;
import com.gcplot.model.gc.GCEvent;
import com.gcplot.repository.GCEventRepository;
import com.google.common.collect.Lists;
import org.apache.cassandra.utils.UUIDGen;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Months;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static com.gcplot.model.gc.cassandra.Mapper.eventFrom;
import static com.gcplot.model.gc.cassandra.Mapper.eventsFrom;

public class CassandraGCEventRepository extends AbstractVMEventsCassandraRepository<GCEvent> implements GCEventRepository {
    protected static final String TABLE_NAME = "gc_event";
    protected static final String DATE_PATTERN = "yyyy-MM";
    public static final String[] NON_KEY_FIELDS = new String[] { "id", "parent_id", "description",
            "occurred", "vm_event_type", "capacity", "total_capacity", "tmstm",
            "pause_mu", "generations", "concurrency", "ext"};
    public static final String[] LAST_EVENT_FIELDS = Utils.concat(NON_KEY_FIELDS, new String[] { "bucket_id" });
    public static final String[] PAUSE_EVENT_FIELDS = new String[] { "occurred", "vm_event_type", "pause_mu", "tmstm", "generations", "concurrency" };

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
        final Iterator<Row> i = events0(analyseId, jvmId, range, NON_KEY_FIELDS).iterator();
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
    public List<GCEvent> pauseEvents(String analyseId, String jvmId, Range range) {
        return eventsFrom(events0(analyseId, jvmId, range, PAUSE_EVENT_FIELDS));
    }

    @Override
    public Iterator<GCEvent> lazyPauseEvents(String analyseId, String jvmId, Range range) {
        final Iterator<Row> i = events0(analyseId, jvmId, range, PAUSE_EVENT_FIELDS).iterator();
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
        for (int i = dates.size() - 1; i >= 0; i--) {
            Select from = QueryBuilder.select(fields).from(TABLE_NAME);
            Select.Where statement = from.limit(1)
                    .where(eq("analyse_id", UUID.fromString(analyseId)))
                    .and(eq("jvm_id", jvmId))
                    .and(eq("date", dates.get(i)));
            if (bucketId != null) {
                from.allowFiltering();
                statement.and(eq("bucket_id", bucketId));
            }
            List<Row> rows = connector.session().execute(statement).all();
            if (rows.size() > 0) {
                return Optional.of(rows.get(0));
            }
        }
        return Optional.empty();
    }

    protected ResultSet events0(String analyseId, String jvmId, Range range, String[] fields) {
        return connector.session().execute(QueryBuilder.select(fields).from(TABLE_NAME)
                .allowFiltering()
                .where(eq("analyse_id", UUID.fromString(analyseId)))
                .and(eq("jvm_id", jvmId))
                .and(in("date", dates(range)))
                .and(gte("occurred", range.from.toDate()))
                .and(lte("occurred", range.to.toDate())).setFetchSize(fetchSize));
    }

    /**
     * Returns a range of dates in format YYYY-MM, starting from the most recent one.
     *
     * @param range
     * @return
     */
    protected List<String> dates(Range range) {
        return IntStream.range(0, Months.monthsBetween(
                range.from.monthOfYear().roundFloorCopy(),
                range.to.monthOfYear().roundCeilingCopy()).getMonths())
                .mapToObj(i -> range.from.plusMonths(i).toString(DATE_PATTERN))
                .collect(Collectors.toList());
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
                .value("occurred", event.occurred().toDate())
                .value("vm_event_type", event.vmEventType().type())
                .value("capacity", Lists.newArrayList(event.capacity().usedBefore(), event.capacity().usedAfter(), event.capacity().total()))
                .value("total_capacity", Lists.newArrayList(event.totalCapacity().usedBefore(), event.totalCapacity().usedAfter(), event.totalCapacity().total()))
                .value("pause_mu", event.pauseMu())
                .value("generations", EnumSetUtils.encode(event.generations()))
                .value("concurrency", event.concurrency().type())
                .value("ext", event.ext()).setConsistencyLevel(ConsistencyLevel.ONE);
    }

}
