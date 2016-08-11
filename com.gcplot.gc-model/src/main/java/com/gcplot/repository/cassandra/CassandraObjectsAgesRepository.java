package com.gcplot.repository.cassandra;

import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.gcplot.commons.Range;
import com.gcplot.model.gc.ObjectsAges;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static com.gcplot.model.gc.cassandra.Mapper.*;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/3/16
 */
public class CassandraObjectsAgesRepository extends AbstractJVMEventsCassandraRepository<ObjectsAges> {
    protected static final String TABLE_NAME = "objects_ages";
    protected static final String[] FIELDS = new String[] { "occurred", "occupied", "total", "ext"};

    @Override
    public List<ObjectsAges> events(String analyseId, String jvmId, Range range) {
        return objectsAgesFrom(events0(analyseId, jvmId, range, FIELDS));
    }

    @Override
    public Iterator<ObjectsAges> lazyEvents(String analyseId, String jvmId, Range range) {
        final Iterator<Row> i = events0(analyseId, jvmId, range, FIELDS).iterator();
        return new Iterator<ObjectsAges>() {
            @Override
            public boolean hasNext() {
                return i.hasNext();
            }

            @Override
            public ObjectsAges next() {
                return objectsAgeFrom(i.next());
            }
        };
    }

    @Override
    public void erase(String analyseId, String jvmId, Range range) {
        connector.session().execute(QueryBuilder.delete().all().from(TABLE_NAME)
                .where(eq("analyse_id", UUID.fromString(analyseId)))
                .and(eq("jvm_id", jvmId)));
    }

    @Override
    public void erase(String analyseId, List<String> jvmIds, Range range) {
        connector.session().execute(QueryBuilder.delete().all().from(TABLE_NAME)
                .where(eq("analyse_id", UUID.fromString(analyseId)))
                .and(in("jvm_id", jvmIds)));
    }

    protected ResultSet events0(String analyseId, String jvmId, Range range, String[] fields) {
        return connector.session().execute(QueryBuilder.select(fields).from(TABLE_NAME)
                .allowFiltering()
                .where(eq("analyse_id", UUID.fromString(analyseId)))
                .and(eq("jvm_id", jvmId))
                .and(gte("occurred", range.from.toDate()))
                .and(lte("occurred", range.to.toDate())).setFetchSize(fetchSize));
    }

    protected RegularStatement addStatement(ObjectsAges oa) {
        return QueryBuilder.insertInto(TABLE_NAME)
                .value("analyse_id", UUID.fromString(oa.analyseId()))
                .value("occurred", oa.occurred().toDate())
                .value("written_at", now())
                .value("jvm_id", oa.jvmId())
                .value("occupied", oa.occupied())
                .value("total", oa.total())
                .value("ext", oa.ext());
    }
}
