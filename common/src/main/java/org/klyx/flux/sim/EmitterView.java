package org.klyx.flux.sim;

public final class EmitterView {
    public static final int MOVED = 1;
    public static final int APPEARANCE = 2;
    public static final int COLOR = 4;

    private static final double MOVE_EPSILON = 1.0 / 32;
    private static final double TICK_SECONDS = 0.05;
    private static final float SIZE_EPSILON = 1.0f / 64;
    private static final float ROLL_EPSILON = 1.0f;
    private static final int COLOR_EPSILON = 12;
    // size and color are a lot more expensive than position, so send it slightly less
    private static final int APPEARANCE_INTERVAL_FACTOR = 2;
    private static final int MAX_RAMP = 20; // ticks
    private static final float MAX_RAMP_DEGREES = 170f;

    private final int updateInterval;
    private final int moveTicks;
    private long tick;
    private final int[] shown;
    private final double[] sentX;
    private final double[] sentY;
    private final double[] sentZ;
    private final float[] sentSize;
    private final float[] sentRoll;
    private final int[] sentRgb;
    private final long[] sentTick;
    private final long[] moveTick;
    private final double[] slopeX;
    private final double[] slopeY;
    private final double[] slopeZ;
    private final float[] sizeSlope;
    private final float[] rollSlope;
    private final int[] ramp;
    private int shownCount;

    public EmitterView(int capacity, int updateInterval, int moveTicks) {
        if (updateInterval < 1) throw new IllegalArgumentException("updateInterval has to be positive");
        if (moveTicks < 1) throw new IllegalArgumentException("moveTicks has to be positive");

        this.updateInterval = updateInterval;
        this.moveTicks = moveTicks;
        this.shown = new int[capacity];
        this.sentX = new double[capacity];
        this.sentY = new double[capacity];
        this.sentZ = new double[capacity];
        this.sentSize = new float[capacity];
        this.sentRoll = new float[capacity];
        this.sentRgb = new int[capacity];
        this.sentTick = new long[capacity];
        this.moveTick = new long[capacity];
        this.slopeX = new double[capacity];
        this.slopeY = new double[capacity];
        this.slopeZ = new double[capacity];
        this.sizeSlope = new float[capacity];
        this.rollSlope = new float[capacity];
        this.ramp = new int[capacity];
    }

    public int shownCount() {
        return this.shownCount;
    }

    public boolean isShown(int slot) {
        return this.shown[slot] != 0;
    }

    public void sync(Emitter emitter, Listener listener) {
        this.tick++;

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

            if ((this.tick + slot) % this.updateInterval != 0) continue;

            boolean checkAppearance = (this.tick + slot) % ((long) this.updateInterval * APPEARANCE_INTERVAL_FACTOR) == 0;
            int changed = changedGroups(emitter, slot, checkAppearance);
            if (changed == 0) continue;

            record(emitter, slot, changed, false);
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
        record(emitter, slot, MOVED | APPEARANCE | COLOR, true);
        listener.spawn(slot);
    }

    private int changedGroups(Emitter emitter, int slot, boolean checkAppearance) {
        int changed = 0;

        if (hasMoved(emitter, slot)) changed |= MOVED;
        if (!checkAppearance) return changed;

        if (hasNewAppearance(emitter, slot)) changed |= APPEARANCE;
        if (hasNewColor(emitter.rgb(slot), this.sentRgb[slot])) changed |= COLOR;

        return changed;
    }

    private boolean hasMoved(Emitter emitter, int slot) {
        long elapsed = this.tick - this.moveTick[slot];
        if (elapsed + this.updateInterval > this.moveTicks && hasGlide(slot)) return true;

        int gliding = (int) Math.min(elapsed, this.moveTicks);

        return Math.abs(emitter.x(slot) - (this.sentX[slot] + this.slopeX[slot] * gliding)) > MOVE_EPSILON
                || Math.abs(emitter.y(slot) - (this.sentY[slot] + this.slopeY[slot] * gliding)) > MOVE_EPSILON
                || Math.abs(emitter.z(slot) - (this.sentZ[slot] + this.slopeZ[slot] * gliding)) > MOVE_EPSILON;
    }

    public boolean hasGlide(int slot) {
        return this.slopeX[slot] != 0 || this.slopeY[slot] != 0 || this.slopeZ[slot] != 0;
    }

    public double targetX(int slot) {
        return this.sentX[slot] + this.slopeX[slot] * this.moveTicks;
    }

    public double targetY(int slot) {
        return this.sentY[slot] + this.slopeY[slot] * this.moveTicks;
    }

    public double targetZ(int slot) {
        return this.sentZ[slot] + this.slopeZ[slot] * this.moveTicks;
    }

    private boolean hasNewAppearance(Emitter emitter, int slot) {
        return Math.abs(emitter.size(slot) - displayedSize(slot)) > SIZE_EPSILON
                || Math.abs(emitter.roll(slot) - displayedRoll(slot)) > ROLL_EPSILON;
    }

    private static double finiteOrZero(double value) {
        return Double.isFinite(value) ? value : 0;
    }

    private int rampFor(float rollSlope) {
        int ticks = MAX_RAMP;
        if (rollSlope != 0) ticks = Math.min(ticks, (int) (MAX_RAMP_DEGREES / Math.abs(rollSlope)));

        return Math.max(ticks, this.updateInterval + 1);
    }

    private int elapsed(int slot) {
        return (int) Math.min(this.tick - this.sentTick[slot], this.ramp[slot]);
    }

    public float targetSize(int slot) {
        return Math.max(0, this.sentSize[slot] + this.sizeSlope[slot] * this.ramp[slot]);
    }

    public float targetRoll(int slot) {
        return this.sentRoll[slot] + this.rollSlope[slot] * this.ramp[slot];
    }

    public int ramp(int slot) {
        return this.ramp[slot];
    }

    public float displayedSize(int slot) {
        return Math.max(0, this.sentSize[slot] + this.sizeSlope[slot] * elapsed(slot));
    }

    public float displayedRoll(int slot) {
        return this.sentRoll[slot] + this.rollSlope[slot] * elapsed(slot);
    }

    public boolean isRamping(int slot) {
        return (this.sizeSlope[slot] != 0 || this.rollSlope[slot] != 0) && elapsed(slot) < this.ramp[slot];
    }

    public int remainingRamp(int slot) {
        return Math.max(1, this.ramp[slot] - elapsed(slot));
    }

    private static boolean hasNewColor(int rgb, int sent) {
        return Math.abs(((rgb >> 16) & 0xFF) - ((sent >> 16) & 0xFF)) > COLOR_EPSILON
                || Math.abs(((rgb >> 8) & 0xFF) - ((sent >> 8) & 0xFF)) > COLOR_EPSILON
                || Math.abs((rgb & 0xFF) - (sent & 0xFF)) > COLOR_EPSILON;
    }

    public void removeAll(SlotConsumer removed) {
        for (int slot = 0; slot < this.shown.length; slot++) {
            if (this.shown[slot] == 0) continue;

            this.shown[slot] = 0;
            removed.accept(slot);
        }

        this.shownCount = 0;
    }

    private void record(Emitter emitter, int slot, int groups, boolean fresh) {
        if ((groups & MOVED) != 0) {
            this.sentX[slot] = emitter.x(slot);
            this.sentY[slot] = emitter.y(slot);
            this.sentZ[slot] = emitter.z(slot);
            this.slopeX[slot] = finiteOrZero(emitter.vx(slot) * TICK_SECONDS);
            this.slopeY[slot] = finiteOrZero(emitter.vy(slot) * TICK_SECONDS);
            this.slopeZ[slot] = finiteOrZero(emitter.vz(slot) * TICK_SECONDS);
            this.moveTick[slot] = this.tick;
        }

        if ((groups & APPEARANCE) != 0) {
            float size = emitter.size(slot);
            float roll = emitter.roll(slot);
            long elapsed = this.tick - this.sentTick[slot];

            if (fresh || elapsed <= 0) {
                this.sizeSlope[slot] = 0;
                this.rollSlope[slot] = 0;
                this.ramp[slot] = this.updateInterval + 1;
            } else {
                this.sizeSlope[slot] = (size - this.sentSize[slot]) / elapsed;
                this.rollSlope[slot] = (roll - this.sentRoll[slot]) / elapsed;
                this.ramp[slot] = rampFor(this.rollSlope[slot]);
            }

            this.sentSize[slot] = size;
            this.sentRoll[slot] = roll;
            this.sentTick[slot] = this.tick;
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
