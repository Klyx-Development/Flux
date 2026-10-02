package org.klyx.flux.sim.emitter;

import org.klyx.flux.expr.Expression;
import org.klyx.flux.math.Vec3;
import org.klyx.flux.sim.Numbers;
import org.klyx.flux.sim.SimVariables;
import org.klyx.flux.sim.spec.EmitterSpec;

import java.util.random.RandomGenerator;

public final class EmissionCycle {
    private static final double DEBT_TOLERANCE = 1.0e-9;

    private final EmitterSpec emitter;
    private final RandomGenerator random;
    private final SimVariables variables;
    private final Spawner spawner;
    private final int capacity;

    private double time;
    private double active;
    private double sleep;
    private boolean burstDone;
    private double spawnDebt;
    private boolean finished;

    public EmissionCycle(EmitterSpec emitter, RandomGenerator random, SimVariables variables, Spawner spawner, int capacity) {
        this.emitter = emitter;
        this.random = random;
        this.variables = variables;
        this.spawner = spawner;
        this.capacity = capacity;
    }

    public double time() {
        return this.time;
    }

    public boolean finished() {
        return this.finished;
    }

    public void begin(boolean reroll) {
        if (reroll) this.variables.rollEmitterRandoms(this.random);

        this.variables.emitterAge(this.time);
        this.variables.emitterLifetime(0);

        switch (this.emitter.lifetime()) {
            case EmitterSpec.Lifetime.Once once -> {
                this.active = Numbers.nonNegative(once.activeTime().eval(this.variables));
                this.sleep = 0;
            }
            case EmitterSpec.Lifetime.Looping looping -> {
                this.active = Numbers.nonNegative(looping.activeTime().eval(this.variables));
                this.sleep = Numbers.nonNegative(looping.sleepTime().eval(this.variables));
                if (this.active + this.sleep < Numbers.MIN_LIFETIME) this.sleep = Numbers.MIN_LIFETIME;
            }
        }

        this.variables.emitterLifetime(this.active);
        this.burstDone = false;
    }

    public void run(double deltaSeconds, Vec3 origin) {
        boolean looping = this.emitter.lifetime() instanceof EmitterSpec.Lifetime.Looping;
        boolean instant = this.emitter.rate() instanceof EmitterSpec.Rate.Instant;
        double remaining = deltaSeconds;
        int cycles = 0;

        while (!this.finished && cycles < 64) {
            if (instant && !this.burstDone) fireBurst(origin);

            double cycleEnd = looping ? this.active + this.sleep : this.active;

            if (this.time < this.active) {
                if (remaining <= 0) break;

                double span = advance(remaining, this.active);

                spawnSteady(span, origin);
                remaining -= span;
            } else if (this.time < cycleEnd) {
                if (remaining <= 0) break;

                remaining -= advance(remaining, cycleEnd);
            } else if (looping) {
                this.time = 0;
                begin(true);
                cycles++;
            } else {
                this.finished = true;
            }
        }
    }

    private void fireBurst(Vec3 origin) {
        this.burstDone = true;
        this.variables.emitterAge(this.time);

        double amount = ((EmitterSpec.Rate.Instant) this.emitter.rate()).count().eval(this.variables);

        this.spawner.spawn(count(amount), origin, this.time);
    }

    private void spawnSteady(double span, Vec3 origin) {
        if (!(this.emitter.rate() instanceof EmitterSpec.Rate.Steady(Expression second))) return;
        this.variables.emitterAge(this.time);

        double perSecond = second.eval(this.variables);
        if (!(perSecond > 0 && Double.isFinite(perSecond))) return;

        this.spawnDebt += perSecond * span;

        // tolerance keeps 0.05 added twenty times (0.9999999999999999) from rounding down to 0
        int whole = (int) Math.min(Math.floor(this.spawnDebt + DEBT_TOLERANCE), this.capacity);

        this.spawner.spawn(whole, origin, this.time);
        this.spawnDebt = Math.max(0, this.spawnDebt - Math.floor(this.spawnDebt + DEBT_TOLERANCE));
    }

    private double advance(double remaining, double boundary) {
        double toBoundary = boundary - this.time;

        if (remaining >= toBoundary) {
            this.time = boundary;
            return toBoundary;
        }

        this.time += remaining;
        return remaining;
    }

    private static int count(double value) {
        return value > 0 && Double.isFinite(value) ? (int) Math.min(value, EmitterSpec.MAX_PARTICLES) : 0;
    }
}
