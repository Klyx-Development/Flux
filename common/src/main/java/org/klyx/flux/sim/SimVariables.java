package org.klyx.flux.sim;

import org.klyx.flux.expr.Script;
import org.klyx.flux.expr.Variables;
import org.klyx.flux.sim.curve.Curve;
import org.klyx.flux.sim.emitter.ParticleStore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.random.RandomGenerator;

public final class SimVariables implements Variables {
    public static final Set<String> NAMES = Set.of(
            "emitter_age", "emitter_lifetime",
            "emitter_random_1", "emitter_random_2", "emitter_random_3", "emitter_random_4",
            "particle_age", "particle_lifetime",
            "particle_random_1", "particle_random_2", "particle_random_3", "particle_random_4",
            "entity_scale"
    );

    private static final int MAX_CURVE_DEPTH = 4;

    private final double[] emitterRandom = new double[4];
    private final double[] particleRandom = new double[4];
    private double emitterAge;
    private double emitterLifetime;
    private double particleAge;
    private double particleLifetime;
    private Map<String, Curve> curves = Map.of();
    private Map<String, double[]> custom = Map.of();
    private int curveDepth;

    public void curves(Map<String, Curve> curves) {
        this.curves = curves;
    }

    public void emitterAge(double emitterAge) {
        this.emitterAge = emitterAge;
    }

    public void emitterLifetime(double emitterLifetime) {
        this.emitterLifetime = emitterLifetime;
    }

    public void particleLifetime(double particleLifetime) {
        this.particleLifetime = particleLifetime;
    }

    public void declare(Script... scripts) {
        Map<String, double[]> holders = new HashMap<>();

        for (Script script : scripts) {
            for (Script.Assignment assignment : script.assignments()) {
                holders.computeIfAbsent(assignment.name(), _ -> new double[1]);
            }
        }

        this.custom = holders;
    }

    public void run(Script script) {
        for (Script.Assignment assignment : script.assignments()) {
            double value = Numbers.finiteOr(assignment.value().eval(this), 0);
            this.custom.get(assignment.name())[0] = value;
        }
    }

    public void rollEmitterRandoms(RandomGenerator random) {
        for (int i = 0; i < 4; i++) {
            this.emitterRandom[i] = random.nextDouble();
        }
    }

    public void clearParticle() {
        this.particleAge = 0;
        this.particleLifetime = 0;
        Arrays.fill(this.particleRandom, 0);
    }

    public void loadParticle(ParticleStore store, int slot, double age, double lifetime, double emitterAge) {
        this.particleAge = age;
        this.particleLifetime = lifetime;
        System.arraycopy(store.randoms, slot * 4, this.particleRandom, 0, 4);
        this.emitterAge = emitterAge;
    }

    public static boolean isKnown(String canonical) {
        int dot = canonical.indexOf('.');

        if (dot < 0) return false;

        String prefix = canonical.substring(0, dot);
        return (prefix.equals("variable") || prefix.equals("query"))
                && NAMES.contains(canonical.substring(dot + 1));
    }

    @Override
    public double get(String name) {
        int dot = name.indexOf('.');

        if (dot > 0) name = name.substring(0, dot + 1) + name.substring(dot + 1);

        return switch (name) {
            case "variable.emitter_age", "query.emitter_age" -> this.emitterAge;
            case "variable.entity_scale", "query.entity_scale" -> 1;
            case "variable.emitter_lifetime", "query.emitter_lifetime" -> this.emitterLifetime;
            case "variable.emitter_random_1", "query.emitter_random_1" -> this.emitterRandom[0];
            case "variable.emitter_random_2", "query.emitter_random_2" -> this.emitterRandom[1];
            case "variable.emitter_random_3", "query.emitter_random_3" -> this.emitterRandom[2];
            case "variable.emitter_random_4", "query.emitter_random_4" -> this.emitterRandom[3];
            case "variable.particle_age", "query.particle_age" -> this.particleAge;
            case "variable.particle_lifetime", "query.particle_lifetime" -> this.particleLifetime;
            case "variable.particle_random_1", "query.particle_random_1" -> this.particleRandom[0];
            case "variable.particle_random_2", "query.particle_random_2" -> this.particleRandom[1];
            case "variable.particle_random_3", "query.particle_random_3" -> this.particleRandom[2];
            case "variable.particle_random_4", "query.particle_random_4" -> this.particleRandom[3];
            default -> {
                double[] holder = this.custom.get(name);
                yield holder != null ? holder[0] : curve(name);
            }
        };
    }

    private double curve(String name) {
        Curve curve = this.curves.get(name);
        if (curve == null || this.curveDepth >= MAX_CURVE_DEPTH) return 0;

        this.curveDepth++;

        try {
            double value = curve.valueAt(curve.input().eval(this), curve.horizontalRange().eval(this));
            return Double.isFinite(value) ? value : 0;
        } finally {
            this.curveDepth--;
        }
    }
}
