package org.klyx.flux.sim.emitter;

import java.util.random.RandomGenerator;

public final class ParticleStore {
    public final int capacity;
    public final boolean[] alive;
    public final double[] x;
    public final double[] y;
    public final double[] z;
    public final double[] vx;
    public final double[] vy;
    public final double[] vz;
    public final double[] age;
    public final double[] lifetime;
    public final float[] size;
    public final float[] roll;
    public final int[] rgb;
    public final int[] generation;
    public final double[] randoms;
    public final double[] anchorX;
    public final double[] anchorY;
    public final double[] anchorZ;
    public final double[] bornX;
    public final double[] bornY;
    public final double[] bornZ;

    private final int[] freeSlots;
    private int freeCount;
    private int highWater;
    private int aliveCount;

    public ParticleStore(int capacity) {
        this.capacity = capacity;
        this.alive = new boolean[capacity];
        this.x = new double[capacity];
        this.y = new double[capacity];
        this.z = new double[capacity];
        this.vx = new double[capacity];
        this.vy = new double[capacity];
        this.vz = new double[capacity];
        this.age = new double[capacity];
        this.lifetime = new double[capacity];
        this.size = new float[capacity];
        this.roll = new float[capacity];
        this.rgb = new int[capacity];
        this.generation = new int[capacity];
        this.randoms = new double[capacity * 4];
        this.anchorX = new double[capacity];
        this.anchorY = new double[capacity];
        this.anchorZ = new double[capacity];
        this.bornX = new double[capacity];
        this.bornY = new double[capacity];
        this.bornZ = new double[capacity];
        this.freeSlots = new int[capacity];
    }

    public int highWater() {
        return this.highWater;
    }

    public int aliveCount() {
        return this.aliveCount;
    }

    public int allocate() {
        if (this.freeCount > 0) return this.freeSlots[--this.freeCount];
        if (this.highWater < this.capacity) return this.highWater++;
        return -1;
    }

    public void release(int slot) {
        this.freeSlots[this.freeCount++] = slot;
    }

    public void activate(int slot, double life) {
        this.alive[slot] = true;
        this.aliveCount++;
        this.generation[slot]++;
        this.age[slot] = 0;
        this.lifetime[slot] = life;
    }

    public void kill(int slot) {
        this.alive[slot] = false;
        this.aliveCount--;
        release(slot);
    }

    public void rollRandoms(int slot, RandomGenerator random) {
        for (int i = 0; i < 4; i++) {
            this.randoms[slot * 4 + i] = random.nextDouble();
        }
    }
}
