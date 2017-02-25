package com.gcplot.repository.cassandra;

import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.gcplot.model.VMEvent;
import com.gcplot.repository.VMEventsRepository;

import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/5/16
 */
public abstract class AbstractVMEventsCassandraRepository<T extends VMEvent> extends AbstractCassandraRepository
        implements VMEventsRepository<T> {

    abstract RegularStatement addStatement(String analyseId, String jvmId, T event);

    @Override
    public void add(String analyseId, String jvmId, T event) {
        connector.session().execute(addStatement(analyseId, jvmId, event));
    }

    @Override
    public void add(String analyseId, String jvmId, List<T> events) {
        connector.session().execute(
                QueryBuilder.batch(events.stream().map(e -> addStatement(analyseId, jvmId, e)).toArray(RegularStatement[]::new)));
    }

    @Override
    public void addAsync(String analyseId, String jvmId, T event) {
        connector.session().executeAsync(addStatement(analyseId, jvmId, event));
    }

    @Override
    public void addAsync(String analyseId, String jvmId, List<T> events) {
        connector.session().executeAsync(
                QueryBuilder.batch(events.stream().map(e -> addStatement(analyseId, jvmId, e)).toArray(RegularStatement[]::new)));
    }

}
