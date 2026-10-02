package org.klyx.flux.sim;

public final class EmitterView {
    public static final int MOVED = 1;
    public static final int APPEARANCE = 2;
    public static final int COLOR = 4;

    private static final double MOVE_EPSILON = 1.0 / 512;
    private static final float SIZE_EPSILON = 1.0f / 1024;
    private static final float ROLL_EPSILON = 0.1f;

    private final int[] shown;
    private final double[] sentX;
    private final double[] sentY;
    private final double[] sentZ;
    private final float[] sentSize;
    private final float[] sentRoll;
    private final int[] sentRgb;
    private int shownCount;

    public EmitterView(int capacity) {
        this.shown = new int[capacity];
        this.sentX = new double[capacity];
        this.sentY = new double[capacity];
        this.sentZ = new double[capacity];
        this.sentSize = new float[capacity];
        this.sentRoll = new float[capacity];
        this.sentRgb = new int[capacity];
    }

    public int shownCount() {
        return this.shownCount;
    }

    public boolean isShown(int slot) {
        return this.shown[slot] != 0;
    }

    public void sync(Emitter emitter, Listener listener) {
        for (int slot = 0; slot < emitter.highWater(); slot++) {
            boolean alive = emitter.isAlive(slot);
            int generation = emitter.generation(slot);

            if (this.shown[slot] != 0 && (!alive || this.shown[slot] != generation)) {
                hide(slot, listener);
            }

            if (!alive) continue;

            if (this.shown[slot] == 0) {
                show(emitter, slot, generation, listener);
                continue;
            }

            int changed = changedGroups(emitter, slot);
            if (changed == 0) continue;

            record(emitter, slot, changed);
            listener.update(slot, changed);
        }
    }

    private void hide(int slot, Listener listener) {
        this.shown[slot] = 0;
        this.shownCount--;
        listener.remove(slot);
    }

    private void show(Emitter emitter, int slot, int generation, Listener listener) {
        this.shown[slot] = generation;
        this.shownCount++;
        record(emitter, slot, MOVED | APPEARANCE | COLOR);
        listener.spawn(slot);
    }

    private int changedGroups(Emitter emitter, int slot) {
        int changed = 0;

        if (hasMoved(emitter, slot)) changed |= MOVED;
        if (hasNewAppearance(emitter, slot)) changed |= APPEARANCE;
        if (emitter.rgb(slot) != this.sentRgb[slot]) changed |= COLOR;

        return changed;
    }

    private boolean hasMoved(Emitter emitter, int slot) {
        return Math.abs(emitter.x(slot) - this.sentX[slot]) > MOVE_EPSILON
                || Math.abs(emitter.y(slot) - this.sentY[slot]) > MOVE_EPSILON
                || Math.abs(emitter.z(slot) - this.sentZ[slot]) > MOVE_EPSILON;
    }

    private boolean hasNewAppearance(Emitter emitter, int slot) {
        return Math.abs(emitter.size(slot) - this.sentSize[slot]) > SIZE_EPSILON
                || Math.abs(emitter.roll(slot) - this.sentRoll[slot]) > ROLL_EPSILON;
    }

    public void removeAll(SlotConsumer removed) {
        for (int slot = 0; slot < this.shown.length; slot++) {
            if (this.shown[slot] == 0) continue;

            this.shown[slot] = 0;
            removed.accept(slot);
        }

        this.shownCount = 0;
    }

    private void record(Emitter emitter, int slot, int groups) {
        if ((groups & MOVED) != 0) {
            this.sentX[slot] = emitter.x(slot);
            this.sentY[slot] = emitter.y(slot);
            this.sentZ[slot] = emitter.z(slot);
        }

        if ((groups & APPEARANCE) != 0) {
            this.sentSize[slot] = emitter.size(slot);
            this.sentRoll[slot] = emitter.roll(slot);
        }

        if ((groups & COLOR) != 0) {
            this.sentRgb[slot] = emitter.rgb(slot);
        }
    }

    public interface Listener {
        void spawn(int slot);
        void update(int slot, int changed);
        void remove(int slot);
    }

    @FunctionalInterface
    public interface SlotConsumer {
        void accept(int slot);
    }
}
