package com.gcplot.triggers;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/1/17
 */
public interface BinaryTrigger extends Trigger<BinaryTrigger.State> {

    enum State {
        ACQUIRED, WAIT
    }

}
