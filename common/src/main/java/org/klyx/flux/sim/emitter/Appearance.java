package org.klyx.flux.sim.emitter;

import org.klyx.flux.expr.Expression;
import org.klyx.flux.sim.Numbers;
import org.klyx.flux.sim.SimVariables;
import org.klyx.flux.sim.spec.ParticleSpec;

import java.util.List;

public final class Appearance {
    private final ParticleSpec particle;
    private final SimVariables variables;
    private final boolean sizeConstant;
    private final boolean spinConstant;
    private final double constantSize;
    private final double constantSpin;

    public Appearance(ParticleSpec particle, SimVariables variables) {
        this.particle = particle;
        this.variables = variables;
        this.sizeConstant = particle.size().isConstant();
        this.spinConstant = particle.spin().isConstant();
        this.constantSize = this.sizeConstant ? particle.size().evalConstant() : 0;
        this.constantSpin = this.spinConstant ? particle.spin().evalConstant() : 0;
    }

    public double size() {
        double value = this.sizeConstant ? this.constantSize : this.particle.size().eval(this.variables);
        return Double.isFinite(value) ? Math.max(0, value) : 0;
    }

    public double spin() {
        double value = this.spinConstant ? this.constantSpin : this.particle.spin().eval(this.variables);
        return Double.isFinite(value) ? value : 0;
    }

    public int color(double particleAge, double particleLifetime) {
        List<Expression> channels = this.particle.colorExpressions();

        if (channels != null) {
            return channel(channels.get(0)) << 16 | channel(channels.get(1)) << 8 | channel(channels.get(2));
        }

        Expression interpolant = this.particle.colorInterpolant();
        double at = interpolant != null
                ? Numbers.finiteOr(interpolant.eval(this.variables), 0)
                : particleAge / particleLifetime;

        return this.particle.color().at(at);
    }

    private int channel(Expression expression) {
        double value = Numbers.finiteOr(expression.eval(this.variables), 0);
        return (int) Math.round(Math.clamp(value, 0, 1) * 255);
    }
}
