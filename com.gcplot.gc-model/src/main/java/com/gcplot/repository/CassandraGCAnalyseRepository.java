package com.gcplot.repository;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;
import com.gcplot.Identifier;
import com.gcplot.cassandra.CassandraConnector;
import com.gcplot.model.gc.GCAnalyse;
import com.gcplot.model.gc.MemoryDetails;
import com.google.common.base.Preconditions;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static com.gcplot.model.gc.cassandra.Mapper.analyseFrom;
import static com.gcplot.model.gc.cassandra.Mapper.analysesFrom;

public class CassandraGCAnalyseRepository extends AbstractCassandraRepository implements GCAnalyseRepository {
    protected static final String TABLE_NAME = "gc_analyse";

    @Override
    public List<GCAnalyse> analyses() {
        Statement statement = QueryBuilder.select().all().from(TABLE_NAME);
        return analysesFrom(connector.session().execute(statement));
    }

    @Override
    public Optional<GCAnalyse> analyse(String id) {
        Statement statement = QueryBuilder.select().all()
                .from(TABLE_NAME).allowFiltering().where(eq("id", UUID.fromString(id)));
        return Optional.ofNullable(analyseFrom(connector.session().execute(statement).one()));
    }

    @Override
    public List<GCAnalyse> analysesFor(Identifier accountId) {
        Statement statement = QueryBuilder.select().all().from(TABLE_NAME)
                .where(eq("account_id", accountId.toString()));
        return analysesFrom(connector.session().execute(statement));
    }

    @Override
    public void newAnalyse(GCAnalyse analyse) {
        Statement insert = QueryBuilder.insertInto(TABLE_NAME).value("id", uuid())
                .value("account_id", analyse.accountId().toString())
                .value("analyse_name", analyse.name())
                .value("is_continuous", analyse.isContinuous())
                .value("start", analyse.start().toDateTime(DateTimeZone.UTC).toDate())
                .value("last_event", analyse.lastEvent().toDateTime(DateTimeZone.UTC).toDate())
                .value("gc_type", analyse.collectorType().type())
                .value("jvm_ids", analyse.jvmIds())
                .value("jvm_headers", analyse.jvmHeaders())
                .value("jvm_md_page_size", analyse.jvmMemoryDetails().entrySet()
                        .stream().collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().pageSize())))
                .value("jvm_md_phys_total", analyse.jvmMemoryDetails().entrySet()
                        .stream().collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().physicalTotal())))
                .value("jvm_md_phys_free", analyse.jvmMemoryDetails().entrySet()
                        .stream().collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().physicalFree())))
                .value("jvm_md_swap_total", analyse.jvmMemoryDetails().entrySet()
                        .stream().collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().swapTotal())))
                .value("jvm_md_swap_free", analyse.jvmMemoryDetails().entrySet()
                        .stream().collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().swapFree())))
                .setConsistencyLevel(ConsistencyLevel.QUORUM);
        connector.session().execute(insert);
    }

    @Override
    public void analyseJvm(Identifier accId, String id, String jvmId, String headers, MemoryDetails memoryDetails) {
        UUID uuid = UUID.fromString(id);
        connector.session().execute(QueryBuilder.batch(updateTable(accId, uuid).with(add("jvm_ids", jvmId)),
                updateTable(accId, uuid).with(put("jvm_headers", jvmId, headers)),
                updateTable(accId, uuid).with(put("jvm_md_page_size", jvmId, memoryDetails.pageSize())),
                updateTable(accId, uuid).with(put("jvm_md_phys_total", jvmId, memoryDetails.physicalTotal())),
                updateTable(accId, uuid).with(put("jvm_md_phys_free", jvmId, memoryDetails.physicalFree())),
                updateTable(accId, uuid).with(put("jvm_md_swap_total", jvmId, memoryDetails.swapTotal())),
                updateTable(accId, uuid).with(put("jvm_md_swap_free", jvmId, memoryDetails.swapFree())))
                .setConsistencyLevel(ConsistencyLevel.QUORUM));
    }

    @Override
    public void removeAnalyse(Identifier accId, String id) {
        connector.session().execute(QueryBuilder.delete().all().from(TABLE_NAME)
                .where(eq("id", UUID.fromString(id))).and(eq("account_id", accId.toString())));
    }

    @Override
    public void updateLastEvent(Identifier accId, String id, DateTime lastEvent) {
        Statement s = QueryBuilder.update(TABLE_NAME).where(eq("id", UUID.fromString(id)))
                .and(eq("account_id", accId.toString()))
                .with(set("last_event", lastEvent.toDateTime(DateTimeZone.UTC).toDate()));
        connector.session().execute(s);
    }

    protected Update.Where updateTable(Identifier accId, UUID uuid) {
        return QueryBuilder.update(TABLE_NAME).where(eq("id", uuid)).and(eq("account_id", accId.toString()));
    }
}
