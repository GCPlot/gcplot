package com.gcplot.triggers;

import com.gcplot.triggers.binary.BinaryTrigger;
import com.gcplot.triggers.binary.State;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/1/17
 */
public class BinaryTriggerImpl extends AbstractTrigger<State> implements BinaryTrigger {

    @Override
    public State state() {
        if (state instanceof String) {
            state = State.valueOf(state.toString());
        }
        return (State) state;
    }

    public void setState(State state) {
        this.state = state;
    }

}
