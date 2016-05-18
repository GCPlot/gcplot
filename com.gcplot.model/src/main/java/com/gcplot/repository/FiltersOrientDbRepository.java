package com.gcplot.repository;

import com.orientechnologies.orient.core.db.OPartitionedDatabasePoolFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

import java.util.List;
import java.util.stream.Collectors;

public class FiltersOrientDbRepository extends AbstractOrientDbRepository implements FiltersRepository {

    public FiltersOrientDbRepository(OrientDbConfig config, OPartitionedDatabasePoolFactory poolFactory) {
        super(config, poolFactory);
    }

    @Override
    protected void init(OObjectDatabaseTx db, OSchema schema) {
        if (schema.getClass("Filter") == null) {
            OClass filterClass = schema.createClass("Filter");
            filterClass.createProperty("type", OType.STRING);
            filterClass.createProperty("value", OType.STRING);
            filterClass.createIndex("filter.type.indices", OClass.INDEX_TYPE.FULLTEXT_HASH_INDEX, "type");
            filterClass.createIndex("filter.value.indices", OClass.INDEX_TYPE.FULLTEXT_HASH_INDEX, "value");
        }
    }

    @Override
    public boolean isFiltered(String type, String value) {
        try (ODatabaseDocumentTx db = docDb()) {
            return db.query(new OSQLSynchQuery<>(String.format("select type from Filter where type=\"%s\"" +
                    " and value=\"%s\"", type, value))).size() > 0;
        }
    }

    @Override
    public void filter(String type, String value) {
        try (ODatabaseDocumentTx db = docDb()) {
            ODocument newFilter = db.newInstance("Filter");
            newFilter.field("type", type);
            newFilter.field("value", value);
            newFilter.save();
        }
    }

    @Override
    public void notFilter(String type, String value) {
        try (ODatabaseDocumentTx db = docDb()) {
            db.command(new OCommandSQL(String.format("delete from Filter where type=\"%s\" and value=\"%s\"",
                    type, value))).execute();
        }
    }

    @Override
    public List<String> getAllFiltered(String type) {
        try (ODatabaseDocumentTx db = docDb()) {
            List<ODocument> r = db.query(new OSQLSynchQuery<>(String.format("select value from Filter where type=\"%s\"", type)));
            return r.stream().map(o -> (String) o.rawField("value")).collect(Collectors.toList());
        }
    }

}
