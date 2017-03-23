package com.gcplot.repository.cassandra;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;
import com.gcplot.Identifier;
import com.gcplot.model.VMVersion;
import com.gcplot.model.gc.GCAnalyse;
import com.gcplot.model.gc.GarbageCollectorType;
import com.gcplot.model.gc.MemoryDetails;
import com.gcplot.model.gc.SourceType;
import com.gcplot.repository.GCAnalyseRepository;
import com.gcplot.repository.operations.analyse.*;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static com.gcplot.commons.CollectionUtils.processMap;
import static com.gcplot.commons.CollectionUtils.transformValue;
import static com.gcplot.model.gc.cassandra.Mapper.analyseFrom;
import static com.gcplot.model.gc.cassandra.Mapper.analysesFrom;

public class CassandraGCAnalyseRepository extends AbstractCassandraRepository implements GCAnalyseRepository {
    private static final Logger LOG = LoggerFactory.getLogger(CassandraGCAnalyseRepository.class);
    protected static final String TABLE_NAME = "gc_analyse";

    @Override
    public List<GCAnalyse> analyses() {
        Statement statement = QueryBuilder.select().all().from(TABLE_NAME);
        return analysesFrom(connector.session().execute(statement));
    }

    @Override
    public Iterable<GCAnalyse> analyses(boolean isContinuous) {
        Statement statement = QueryBuilder.select().all().from(TABLE_NAME)
                .where(eq("is_continuous", isContinuous));
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
    public Optional<GCAnalyse> analyse(Identifier accountId, String id) {
        Statement statement = QueryBuilder.select().all()
                .from(TABLE_NAME).where(eq("id", UUID.fromString(id))).and(eq("account_id", accountId.toString()));
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
        UUID newId = analyse.id() == null ? UUID.randomUUID() : UUID.fromString(analyse.id());
        Statement insert = QueryBuilder.insertInto(TABLE_NAME).value("id", newId)
                .value("account_id", analyse.accountId().toString())
                .value("analyse_name", analyse.name())
                .value("timezone", analyse.timezone())
                .value("is_continuous", analyse.isContinuous())
                .value("start", analyse.start().toDateTime(DateTimeZone.UTC).getMillis())
                .value("first_event", analyse.firstEvent() != null ? transformValue(analyse.firstEvent(),
                        v -> v.toDateTime(DateTimeZone.UTC).getMillis()) : Collections.emptyMap())
                .value("last_event", analyse.lastEvent() != null ? transformValue(analyse.lastEvent(),
                        v -> v.toDateTime(DateTimeZone.UTC).getMillis()) : Collections.emptyMap())
                .value("jvm_versions", analyse.jvmVersions() != null ?
                        transformValue(analyse.jvmVersions(), VMVersion::type) : Collections.emptyMap())
                .value("jvm_gc_types", analyse.jvmGCTypes() != null ?
                        transformValue(analyse.jvmGCTypes(), GarbageCollectorType::type) : Collections.emptyMap())
                .value("rc_source_type", analyse.sourceType().getUrn())
                .value("rc_source_config_string", Strings.nullToEmpty(analyse.sourceConfig()))
                .value("jvm_rc_source_type", processMap(analyse.sourceByJvm(), SourceType::getUrn))
                .value("jvm_rc_source_config_string", processMap(analyse.sourceConfigByJvm(), Strings::nullToEmpty))
                .value("jvm_ids", analyse.jvmIds() != null ? analyse.jvmIds() : Collections.emptySet())
                .value("jvm_names", analyse.jvmNames() != null ? analyse.jvmNames() : Collections.emptyMap())
                .value("jvm_headers", analyse.jvmHeaders() != null ? analyse.jvmHeaders() : Collections.emptyMap())
                .value("jvm_md_page_size", processMap(analyse.jvmMemoryDetails(), MemoryDetails::pageSize))
                .value("jvm_md_phys_total", processMap(analyse.jvmMemoryDetails(), MemoryDetails::physicalTotal))
                .value("jvm_md_phys_free", processMap(analyse.jvmMemoryDetails(), MemoryDetails::physicalFree))
                .value("jvm_md_swap_total", processMap(analyse.jvmMemoryDetails(), MemoryDetails::swapTotal))
                .value("jvm_md_swap_free", processMap(analyse.jvmMemoryDetails(), MemoryDetails::swapFree))
                .value("ext", analyse.ext())
                .setConsistencyLevel(ConsistencyLevel.ALL);
        connector.session().execute(insert);
        return newId.toString();
    }

    @Override
    public void perform(AnalyseOperation operation) {
        perform(Collections.singletonList(operation));
    }

    @Override
    public void perform(List<AnalyseOperation> operations) {
        List<RegularStatement> statements = new ArrayList<>(2);
        for (AnalyseOperation op : operations) {
            switch (op.type()) {
                case UPDATE_ANALYSE: {
                    UpdateAnalyseOperation uao = (UpdateAnalyseOperation) op;
                    statements.add(updateAnalyse(uao.accountId(), uao.analyseId(), uao.getName(),
                            uao.getTimezone(), uao.getExt()));
                    break;
                }
                case ADD_JVM: {
                    AddJvmOperation ajo = (AddJvmOperation) op;
                    statements.addAll(addJvm(ajo.accountId(), ajo.analyseId(), ajo.getJvmId(), ajo.getJvmName(),
                            ajo.getVersion(), ajo.getType(), ajo.getHeaders(), ajo.getMemoryDetails()));
                    break;
                }
                case UPDATE_JVM_INFO: {
                    UpdateJvmInfoOperation uji = (UpdateJvmInfoOperation) op;
                    statements.addAll(updateJvmInfo(uji.accountId(), uji.analyseId(), uji.getJvmId(), uji.getHeaders(), uji.getMemoryDetails()));
                    break;
                }
                case UPDATE_JVM_VERSION: {
                    UpdateJvmVersionOperation ujv = (UpdateJvmVersionOperation) op;
                    statements.addAll(updateJvmVersion(ujv.accountId(), ujv.analyseId(), ujv.getJvmId(),
                            ujv.getJvmName(), ujv.getVersion(), ujv.getType()));
                    break;
                }
                case REMOVE_JVM: {
                    RemoveJvmOperation rjo = (RemoveJvmOperation) op;
                    statements.addAll(removeJvm(rjo.accountId(), rjo.analyseId(), rjo.getJvmId()));
                    break;
                }
                case REMOVE_ANALYSE: {
                    RemoveAnalyseOperation rao = (RemoveAnalyseOperation) op;
                    statements.add(removeAnalyse(rao.accountId(), rao.analyseId()));
                    break;
                }
                case UPDATE_CORNER_EVENTS: {
                    UpdateCornerEventsOperation ule = (UpdateCornerEventsOperation) op;
                    statements.add(updateCornerEvents(ule.accountId(), ule.analyseId(), ule.getJvmId(),
                            ule.getFirstEvent(), ule.getLastEvent()));
                    break;
                }
                case UPDATE_ANALYZE_SOURCE: {
                    UpdateAnalyzeSourceOperation opp = (UpdateAnalyzeSourceOperation) op;
                    statements.add(updateSource(opp.accountId(), opp.analyseId(), opp.getSourceType(),
                            opp.getSourceConfig()));
                    break;
                }
            }
        }
        if (statements.size() > 1) {
            connector.session().execute(QueryBuilder.batch(statements.toArray(new RegularStatement[statements.size()])));
        } else {
            connector.session().execute(statements.get(0));
        }
    }

    private RegularStatement updateAnalyse(Identifier accountId, String analyseId, String name, String timezone,
                                           String ext) {
        Preconditions.checkNotNull(name, "Analyse name can't be null.");
        UUID uuid = UUID.fromString(analyseId);
        Update.Assignments query = updateTable(accountId, uuid).with(set("analyse_name", name));
        if (!Strings.isNullOrEmpty(timezone)) {
            query = query.and(set("timezone", timezone));
        }
        if (ext != null) {
            query = query.and(set("ext", ext));
        }
        return query;
    }

    private List<RegularStatement> addJvm(Identifier accId, String analyseId, String jvmId,
                            String jvmName, VMVersion version, GarbageCollectorType type,
                            String headers, MemoryDetails memoryDetails) {
        UUID uuid = UUID.fromString(analyseId);
        List<RegularStatement> batch = new ArrayList<>();
        batch.add(updateTable(accId, uuid).with(add("jvm_ids", jvmId)));
        batch.add(updateTable(accId, uuid).with(put("jvm_names", jvmId, jvmName)));
        batch.add(updateTable(accId, uuid).with(put("jvm_headers", jvmId, headers)));
        batch.add(updateTable(accId, uuid).with(put("jvm_versions", jvmId, version.type())));
        batch.add(updateTable(accId, uuid).with(put("jvm_gc_types", jvmId, type.type())));
        if (memoryDetails != null) {
            insertMemoryDetails(accId, jvmId, memoryDetails, uuid, batch);
        }
        return batch;
    }

    private List<RegularStatement> updateJvmInfo(Identifier accId, String analyseId,
                                   String jvmId, String headers, MemoryDetails memoryDetails) {
        Preconditions.checkState(!(headers == null && memoryDetails == null), "Nothing to update.");
        UUID uuid = UUID.fromString(analyseId);
        List<RegularStatement> batch = new ArrayList<>();
        if (headers != null) {
            batch.add(updateTable(accId, uuid).with(put("jvm_headers", jvmId, headers)));
        }
        if (memoryDetails != null) {
            insertMemoryDetails(accId, jvmId, memoryDetails, uuid, batch);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateJvmInfo: {}", batch);
        }
        return batch;
    }

    private List<RegularStatement> updateJvmVersion(Identifier accId, String analyseId, String jvmId,
                                      String jvmName, VMVersion version, GarbageCollectorType type) {
        Preconditions.checkState(!(version == null && type == null && jvmName == null), "Nothing to update.");
        UUID uuid = UUID.fromString(analyseId);
        List<RegularStatement> batch = new ArrayList<>();
        if (jvmName != null) {
            batch.add(updateTable(accId, uuid).with(put("jvm_names", jvmId, jvmName)));
        }
        if (version != null) {
            batch.add(updateTable(accId, uuid).with(put("jvm_versions", jvmId, version.type())));
        }
        if (type != null) {
            batch.add(updateTable(accId, uuid).with(put("jvm_gc_types", jvmId, type.type())));
        }
        return batch;
    }

    private List<RegularStatement> removeJvm(Identifier accId, String analyseId, String jvmId) {
        UUID uuid = UUID.fromString(analyseId);
        return Arrays.asList(delete(accId, uuid, "jvm_ids", jvmId),
                delete(accId, uuid, "jvm_names", jvmId),
                delete(accId, uuid, "last_event", jvmId),
                delete(accId, uuid, "first_event", jvmId),
                delete(accId, uuid, "jvm_headers", jvmId),
                delete(accId, uuid, "jvm_versions", jvmId),
                delete(accId, uuid, "jvm_gc_types", jvmId),
                delete(accId, uuid, "jvm_md_page_size", jvmId),
                delete(accId, uuid, "jvm_md_phys_total", jvmId),
                delete(accId, uuid, "jvm_md_phys_free", jvmId),
                delete(accId, uuid, "jvm_md_swap_total", jvmId),
                delete(accId, uuid, "jvm_md_swap_free", jvmId));
    }

    private Delete.Where removeAnalyse(Identifier accId, String analyseId) {
        return QueryBuilder.delete().all().from(TABLE_NAME)
                .where(eq("id", UUID.fromString(analyseId))).and(eq("account_id", accId.toString()));
    }

    private RegularStatement updateCornerEvents(Identifier accId, String analyseId, String jvmId,
                                               DateTime firstEvent, DateTime lastEvent) {
        RegularStatement ule = updateTable(accId, analyseId)
                .with(put("last_event", jvmId, lastEvent.toDateTime(DateTimeZone.UTC).getMillis()));
        if (firstEvent != null) {
            Update.Where firstEventQuery = updateTable(accId, analyseId);
            firstEventQuery
                    .with(put("first_event", jvmId, firstEvent.toDateTime(DateTimeZone.UTC).getMillis()));
            firstEventQuery.onlyIf(eq("first_event['" + jvmId + "']", null));
            connector.session().execute(firstEventQuery);
        }
        return ule;
    }

    private RegularStatement updateSource(Identifier accId, String analyzeId, SourceType sourceType, String sourceConfig) {
        return updateTable(accId, analyzeId).with(set("rc_source_type", sourceType.getUrn()))
                .and(set("rc_source_config_string", sourceConfig));

    }

    private void insertMemoryDetails(Identifier accId, String jvmId, MemoryDetails memoryDetails, UUID uuid, List<RegularStatement> batch) {
        batch.add(updateTable(accId, uuid).with(put("jvm_md_page_size", jvmId, memoryDetails.pageSize())));
        batch.add(updateTable(accId, uuid).with(put("jvm_md_phys_total", jvmId, memoryDetails.physicalTotal())));
        batch.add(updateTable(accId, uuid).with(put("jvm_md_phys_free", jvmId, memoryDetails.physicalFree())));
        batch.add(updateTable(accId, uuid).with(put("jvm_md_swap_total", jvmId, memoryDetails.swapTotal())));
        batch.add(updateTable(accId, uuid).with(put("jvm_md_swap_free", jvmId, memoryDetails.swapFree())));
    }

    private Update.Where updateTable(Identifier accId, String uuid) {
        return updateTable(accId, UUID.fromString(uuid));
    }

    private Update.Where updateTable(Identifier accId, UUID uuid) {
        return QueryBuilder.update(TABLE_NAME).where(eq("id", uuid)).and(eq("account_id", accId.toString()));
    }

    protected Delete.Where delete(Identifier accId, UUID uuid, String column, Object key) {
        return QueryBuilder.delete().mapElt(column, key).from(TABLE_NAME).where(eq("id", uuid)).and(eq("account_id", accId.toString()));
    }
}
