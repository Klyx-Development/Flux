package org.klyx.flux.paper.display.viewer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ViewerSent {
    private final ConcurrentHashMap<UUID, double[]> positions = new ConcurrentHashMap<>();
    private final int slots;

    public ViewerSent(int slots) {
        this.slots = slots;
    }

    public double[] of(UUID viewer) {
        return this.positions.computeIfAbsent(viewer, _ -> new double[this.slots * 3]);
    }

    public void forget(UUID viewer) {
        this.positions.remove(viewer);
    }

    public void clear() {
        this.positions.clear();
    }

    public static void record(double[] sent, int slot, double x, double y, double z) {
        int base = slot * 3;
        sent[base] = x;
        sent[base + 1] = y;
        sent[base + 2] = z;
    }

    public static boolean movedMoreThan(double[] sent, int slot, double x, double y, double z, double tolerance) {
        int base = slot * 3;
        return Math.abs(x - sent[base]) > tolerance
                || Math.abs(y - sent[base + 1]) > tolerance
                || Math.abs(z - sent[base + 2]) > tolerance;
    }
}
