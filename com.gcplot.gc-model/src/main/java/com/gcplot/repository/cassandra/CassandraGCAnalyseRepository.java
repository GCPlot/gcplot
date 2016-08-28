package com.gcplot.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;
import com.gcplot.Identifier;
import com.gcplot.model.VMVersion;
import com.gcplot.model.gc.GCAnalyse;
import com.gcplot.model.gc.GarbageCollectorType;
import com.gcplot.model.gc.MemoryDetails;
import com.gcplot.repository.GCAnalyseRepository;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.*;
import java.util.function.Function;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static com.gcplot.commons.CollectionUtils.*;
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
    public OptionalLong analysesCount(Identifier accountId) {
        Statement statement = QueryBuilder.select().countAll()
                .from(TABLE_NAME).where(eq("account_id", accountId.toString()));
        ResultSet rs = connector.session().execute(statement);
        Iterator<Row> r = rs.iterator();
        if (r.hasNext()) {
            return OptionalLong.of(r.next().getLong(0));
        } else {
            return OptionalLong.empty();
        }
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
    public String newAnalyse(GCAnalyse analyse) {
        UUID newId = UUID.randomUUID();
        Statement insert = QueryBuilder.insertInto(TABLE_NAME).value("id", newId)
                .value("account_id", analyse.accountId().toString())
                .value("analyse_name", analyse.name())
                .value("is_continuous", analyse.isContinuous())
                .value("start", analyse.start().toDateTime(DateTimeZone.UTC).toDate())
                .value("last_event", analyse.lastEvent() != null ? analyse.lastEvent().toDateTime(DateTimeZone.UTC).toDate() :
                        analyse.start().toDateTime(DateTimeZone.UTC).toDate())
                .value("jvm_versions", analyse.jvmVersions() != null ?
                        transformValue(analyse.jvmVersions(), VMVersion::type) : Collections.emptyMap())
                .value("jvm_gc_types", analyse.jvmGCTypes() != null ?
                        transformValue(analyse.jvmGCTypes(), GarbageCollectorType::type) : Collections.emptyMap())
                .value("jvm_ids", analyse.jvmIds() != null ? analyse.jvmIds() : Collections.emptySet())
                .value("jvm_headers", analyse.jvmHeaders() != null ? analyse.jvmHeaders() : Collections.emptyMap())
                .value("jvm_md_page_size", memoryMap(analyse, MemoryDetails::pageSize))
                .value("jvm_md_phys_total", memoryMap(analyse, MemoryDetails::physicalTotal))
                .value("jvm_md_phys_free", memoryMap(analyse, MemoryDetails::physicalFree))
                .value("jvm_md_swap_total", memoryMap(analyse, MemoryDetails::swapTotal))
                .value("jvm_md_swap_free", memoryMap(analyse, MemoryDetails::swapFree))
                .value("ext", analyse.ext())
                .setConsistencyLevel(ConsistencyLevel.ALL);
        connector.session().execute(insert);
        return newId.toString();
    }

    @Override
    public void addJvm(Identifier accId, String analyseId, String jvmId,
                       VMVersion version, GarbageCollectorType type,
                       String headers, MemoryDetails memoryDetails) {
        UUID uuid = UUID.fromString(analyseId);
        connector.session().execute(QueryBuilder.batch(updateTable(accId, uuid).with(add("jvm_ids", jvmId)),
                updateTable(accId, uuid).with(put("jvm_headers", jvmId, headers)),
                updateTable(accId, uuid).with(put("jvm_versions", jvmId, version.type())),
                updateTable(accId, uuid).with(put("jvm_gc_types", jvmId, type.type())),
                updateTable(accId, uuid).with(put("jvm_md_page_size", jvmId, memoryDetails.pageSize())),
                updateTable(accId, uuid).with(put("jvm_md_phys_total", jvmId, memoryDetails.physicalTotal())),
                updateTable(accId, uuid).with(put("jvm_md_phys_free", jvmId, memoryDetails.physicalFree())),
                updateTable(accId, uuid).with(put("jvm_md_swap_total", jvmId, memoryDetails.swapTotal())),
                updateTable(accId, uuid).with(put("jvm_md_swap_free", jvmId, memoryDetails.swapFree())))
                .setConsistencyLevel(ConsistencyLevel.ALL));
    }

    @Override
    public void removeJvm(Identifier accId, String analyseId, String jvmId) {
        UUID uuid = UUID.fromString(analyseId);
        connector.session().execute(QueryBuilder.batch(delete(accId, uuid, "jvm_ids", jvmId),
                delete(accId, uuid, "jvm_headers", jvmId),
                delete(accId, uuid, "jvm_versions", jvmId),
                delete(accId, uuid, "jvm_gc_types", jvmId),
                delete(accId, uuid, "jvm_md_page_size", jvmId),
                delete(accId, uuid, "jvm_md_phys_total", jvmId),
                delete(accId, uuid, "jvm_md_phys_free", jvmId),
                delete(accId, uuid, "jvm_md_swap_total", jvmId),
                delete(accId, uuid, "jvm_md_swap_free", jvmId))
                .setConsistencyLevel(ConsistencyLevel.ALL));
    }

    @Override
    public void removeAnalyse(Identifier accId, String analyseId) {
        connector.session().execute(QueryBuilder.delete().all().from(TABLE_NAME)
                .where(eq("id", UUID.fromString(analyseId))).and(eq("account_id", accId.toString())));
    }

    @Override
    public void updateLastEvent(Identifier accId, String analyseId, DateTime lastEvent) {
        Statement s = QueryBuilder.update(TABLE_NAME).where(eq("id", UUID.fromString(analyseId)))
                .and(eq("account_id", accId.toString()))
                .with(set("last_event", lastEvent.toDateTime(DateTimeZone.UTC).toDate()));
        connector.session().execute(s);
    }

    protected Update.Where updateTable(Identifier accId, UUID uuid) {
        return QueryBuilder.update(TABLE_NAME).where(eq("id", uuid)).and(eq("account_id", accId.toString()));
    }

    protected Delete.Where delete(Identifier accId, UUID uuid, String column, Object key) {
        return QueryBuilder.delete().mapElt(column, key).from(TABLE_NAME).where(eq("id", uuid)).and(eq("account_id", accId.toString()));
    }

    private Object memoryMap(GCAnalyse analyse, Function<? super MemoryDetails, ? extends Long> valueMapper) {
        return analyse.jvmMemoryDetails() != null ? transformValue(analyse.jvmMemoryDetails(), valueMapper::apply) : Collections.emptyMap();
    }
}
