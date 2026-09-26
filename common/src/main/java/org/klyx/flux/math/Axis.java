package org.klyx.flux.math;

public enum Axis {
    X(0),
    Y(1),
    Z(2);

    private final int index;

    Axis(int index) {
        this.index = index;
    }

    public int index() {
        return this.index;
    }
}
