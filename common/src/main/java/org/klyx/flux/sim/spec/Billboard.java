package org.klyx.flux.sim.spec;

public enum Billboard {
    FIXED(0),
    VERTICAL(1),
    HORIZONTAL(2),
    CENTER(3);

    private final byte id;

    Billboard(int id) {
        this.id = (byte) id;
    }

    public byte id() {
        return this.id;
    }
}
