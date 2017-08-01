package com.gcplot.triggers;

import java.util.Map;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/1/17
 */
public class BinaryTriggerImpl extends AbstractTrigger implements BinaryTrigger {
    private State previousState;
    private State state;

    @Override
    public State previousState() {
        return previousState;
    }

    @Override
    public State state() {
        return state;
    }
}
