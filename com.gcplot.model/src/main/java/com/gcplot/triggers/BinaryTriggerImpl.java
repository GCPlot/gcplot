package com.gcplot.triggers;

import com.gcplot.triggers.binary.BinaryTrigger;
import com.gcplot.triggers.binary.State;

import javax.persistence.Transient;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/1/17
 */
public class BinaryTriggerImpl extends AbstractTrigger<State> implements BinaryTrigger {
    @Transient
    private transient Object previousStateE;
    @Transient
    private transient Object stateE;
    private String previousState;
    private String state;

    @Override
    public State previousState() {
        if (previousStateE == null) {
            previousStateE = State.valueOf(previousState.toUpperCase());
        }
        return (State) previousStateE;
    }

    @Override
    public State state() {
        if (stateE == null) {
            stateE = State.valueOf(state.toUpperCase());
        }
        return (State) stateE;
    }

    public void setPreviousState(State previousState) {
        this.previousStateE = previousState;
        this.previousState = previousState.toString();
    }

    public void setState(State state) {
        this.stateE = state;
        this.state = state.toString();
    }

}
