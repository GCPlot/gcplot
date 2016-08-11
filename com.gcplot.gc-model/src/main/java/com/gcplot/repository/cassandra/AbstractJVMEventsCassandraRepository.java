package com.gcplot.repository.cassandra;

import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.gcplot.model.JVMEvent;
import com.gcplot.repository.JVMEventsRepository;

import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/5/16
 */
public abstract class AbstractJVMEventsCassandraRepository<T extends JVMEvent> extends AbstractCassandraRepository
        implements JVMEventsRepository<T> {

    abstract RegularStatement addStatement(T event);

    @Override
    public void add(T event) {
        connector.session().execute(addStatement(event));
    }

    @Override
    public void add(List<T> events) {
        connector.session().execute(
                QueryBuilder.batch(events.stream().map(this::addStatement).toArray(RegularStatement[]::new)));
    }

    @Override
    public void addAsync(T event) {
        connector.session().executeAsync(addStatement(event));
    }

    @Override
    public void addAsync(List<T> events) {
        connector.session().executeAsync(
                QueryBuilder.batch(events.stream().map(this::addStatement).toArray(RegularStatement[]::new)));
    }

}
