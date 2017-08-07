package com.gcplot.triggers.binary;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 8/7/17
 */
public enum State {
    ACQUIRED(0), WAIT(1);

    private int id;
    public int getId() {
        return id;
    }

    State(int id) {
        this.id = id;
    }
}
