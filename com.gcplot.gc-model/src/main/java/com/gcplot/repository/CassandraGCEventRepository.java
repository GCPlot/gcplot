package com.gcplot.repository;

import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.gcplot.cassandra.CassandraConnector;
import com.gcplot.commons.Range;
import com.gcplot.commons.enums.EnumSetUtils;
import com.gcplot.model.gc.GCEvent;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.joda.time.Months;

import static com.gcplot.model.gc.cassandra.Mapper.*;
import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CassandraGCEventRepository implements GCEventRepository {
    protected static final String TABLE_NAME = "gc_event";
    protected static final String DATE_PATTERN = "yyyy-MM";
    public static final String[] NON_KEY_FIELDS = new String[]{"id", "parent_id", "description",
            "occurred", "vm_event_type", "capacity", "total_capacity",
            "pause_mu", "duration_mu", "generations", "concurrency",
            "ext"};

    public void init() {
        Preconditions.checkNotNull(connector, "Cassandra connector is required.");
    }


    @Override
    public List<GCEvent> events(String analyseId, String jvmId, Range range) {
        return eventsFrom(connector.session().execute(QueryBuilder.select(NON_KEY_FIELDS).from(TABLE_NAME)
                .where(eq("analyse_id", UUID.fromString(analyseId)))
                .and(eq("jvm_id", jvmId))
                .and(in("date", dates(range)))));
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

    @Override
    public void add(GCEvent event) {
        connector.session().execute(addStatement(event));
    }

    @Override
    public void add(List<GCEvent> events) {
        connector.session().execute(
                QueryBuilder.batch((RegularStatement[]) events.stream().map(this::addStatement).toArray()));
    }

    protected List<String> dates(Range range) {
        return IntStream.range(0, Months.monthsBetween(range.from, range.to).getMonths() + 1)
                .mapToObj(i -> range.from.plusDays(i).toString(DATE_PATTERN)).collect(Collectors.toList());
    }

    protected Statement addStatement(GCEvent event) {
        return QueryBuilder.insertInto(TABLE_NAME).value("id", event.id() != null ? UUID.fromString(event.id()) : uuid())
                .value("parent_id", event.parentEvent().isPresent() ? UUID.fromString(event.parentEvent().orElse(null)) : null)
                .value("analyse_id", UUID.fromString(event.analyseId()))
                .value("date", event.occurred().toString(DATE_PATTERN))
                .value("jvm_id", event.jvmId())
                .value("description", event.description())
                .value("occurred", event.occurred().toDate())
                .value("vm_event_type", event.vmEventType().type())
                .value("capacity", Lists.newArrayList(event.capacity().usedBefore(), event.capacity().usedAfter(), event.capacity().total()))
                .value("total_capacity", Lists.newArrayList(event.totalCapacity().usedBefore(), event.totalCapacity().usedAfter(), event.totalCapacity().total()))
                .value("pause_mu", event.pauseMu())
                .value("duration_mu", event.durationMu())
                .value("generations", EnumSetUtils.encode(event.generations()))
                .value("concurrency", event.concurrency().type())
                .value("ext", event.ext());
    }

    public CassandraGCEventRepository(CassandraConnector connector) {
        this.connector = connector;
    }

    protected CassandraConnector connector;
    public CassandraConnector getConnector() {
        return connector;
    }
    public void setConnector(CassandraConnector connector) {
        this.connector = connector;
    }
}
