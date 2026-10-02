package org.klyx.flux.sim.spec;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.expr.Expression;
import org.klyx.flux.math.Vec3;
import org.klyx.flux.shader.Gradient;
import org.klyx.flux.sim.collision.Collision;
import org.klyx.flux.sim.force.Force;

import java.util.ArrayList;
import java.util.List;

public record ParticleSpec(Dynamics dynamics, Look look, Tinting tinting, Display display) {
    public static final String DEFAULT_ITEM = "minecraft:leather_horse_armor";

    public Expression lifetime() {
        return this.dynamics.lifetime();
    }

    public Expression speed() {
        return this.dynamics.speed();
    }

    public @Nullable List<Expression> axisSpeed() {
        return this.dynamics.axisSpeed();
    }

    public List<Expression> acceleration() {
        return this.dynamics.acceleration();
    }

    public Expression drag() {
        return this.dynamics.drag();
    }

    public List<Force> forces() {
        return this.dynamics.forces();
    }

    public @Nullable Collision collision() {
        return this.dynamics.collision();
    }

    public @Nullable Parametric parametric() {
        return this.dynamics.parametric();
    }

    public Expression size() {
        return this.look.size();
    }

    public Expression spin() {
        return this.look.spin();
    }

    public Expression initialRoll() {
        return this.look.initialRoll();
    }

    public Billboard billboard() {
        return this.look.billboard();
    }

    public boolean emissive() {
        return this.look.emissive();
    }

    public double viewRange() {
        return this.look.viewRange();
    }

    public Gradient color() {
        return this.tinting.color();
    }

    public @Nullable Expression colorInterpolant() {
        return this.tinting.colorInterpolant();
    }

    public @Nullable List<Expression> colorExpressions() {
        return this.tinting.colorExpressions();
    }

    public DisplayKind kind() {
        return this.display.kind();
    }

    public String item() {
        return this.display.item();
    }

    public @Nullable String model() {
        return this.display.model();
    }

    public boolean tint() {
        return this.display.tint();
    }

    public static ParticleSpec basic() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        Builder builder = new Builder();

        builder.lifetime = this.lifetime();
        builder.speed = this.speed();
        builder.axisSpeed = this.axisSpeed();
        builder.acceleration = this.acceleration();
        builder.drag = this.drag();
        builder.forces = this.forces();
        builder.collision = this.collision();
        builder.parametric = this.parametric();
        builder.size = this.size();
        builder.spin = this.spin();
        builder.initialRoll = this.initialRoll();
        builder.billboard = this.billboard();
        builder.emissive = this.emissive();
        builder.viewRange = this.viewRange();
        builder.color = this.color();
        builder.colorInterpolant = this.colorInterpolant();
        builder.colorExpressions = this.colorExpressions();
        builder.kind = this.kind();
        builder.item = this.item();
        builder.model = this.model();
        builder.tint = this.tint();

        return builder;
    }

    public ParticleSpec lifetime(Expression lifetime) {
        return toBuilder().lifetime(lifetime).build();
    }

    public ParticleSpec speed(Expression speed) {
        return toBuilder().speed(speed).build();
    }

    public ParticleSpec acceleration(Vec3 acceleration) {
        return toBuilder().acceleration(acceleration).build();
    }

    public ParticleSpec drag(double drag) {
        if (!(drag >= 0) || !Double.isFinite(drag)) {
            throw new IllegalArgumentException("drag must be zero or more and finite: " + drag);
        }

        return toBuilder().drag(Expression.constant(drag)).build();
    }

    public ParticleSpec force(Force force) {
        return toBuilder().force(force).build();
    }

    public ParticleSpec collision(@Nullable Collision collision) {
        return toBuilder().collision(collision).build();
    }

    public ParticleSpec size(Expression size) {
        return toBuilder().size(size).build();
    }

    public ParticleSpec spin(Expression spin) {
        return toBuilder().spin(spin).build();
    }

    public ParticleSpec color(Gradient color) {
        return toBuilder().color(color).build();
    }

    public ParticleSpec billboard(Billboard billboard) {
        return toBuilder().billboard(billboard).build();
    }

    public ParticleSpec emissive(boolean emissive) {
        return toBuilder().emissive(emissive).build();
    }

    public ParticleSpec viewRange(double viewRange) {
        return toBuilder().viewRange(viewRange).build();
    }

    public ParticleSpec item(String item, @Nullable String model, boolean tint) {
        return toBuilder().kind(DisplayKind.ITEM).item(item).model(model).tint(tint).build();
    }

    public static final class Builder {
        private Expression lifetime = Expression.constant(1);
        private Expression speed = Expression.constant(0);
        private @Nullable List<Expression> axisSpeed;
        private List<Expression> acceleration = List.of(
                Expression.constant(0), Expression.constant(0), Expression.constant(0)
        );
        private Expression drag = Expression.constant(0);
        private List<Force> forces = List.of();
        private @Nullable Collision collision;
        private @Nullable Parametric parametric;
        private Expression size = Expression.constant(0.25);
        private Expression spin = Expression.constant(0);
        private Expression initialRoll = Expression.constant(0);
        private Gradient color = Gradient.of(0xFFFFFF);
        private @Nullable Expression colorInterpolant;
        private @Nullable List<Expression> colorExpressions;
        private Billboard billboard = Billboard.CENTER;
        private DisplayKind kind = DisplayKind.TEXT;
        private String item = DEFAULT_ITEM;
        private @Nullable String model;
        private boolean tint = true;
        private boolean emissive = true;
        private double viewRange = 0.4;

        public Builder lifetime(Expression lifetime) {
            this.lifetime = lifetime;
            return this;
        }

        public Builder speed(Expression speed) {
            this.speed = speed;
            return this;
        }

        public Builder axisSpeed(@Nullable List<Expression> axisSpeed) {
            this.axisSpeed = axisSpeed;
            return this;
        }

        public Builder acceleration(List<Expression> acceleration) {
            this.acceleration = acceleration;
            return this;
        }

        public Builder acceleration(Vec3 acceleration) {
            this.acceleration = List.of(
                    Expression.constant(acceleration.x()),
                    Expression.constant(acceleration.y()),
                    Expression.constant(acceleration.z())
            );
            return this;
        }

        public Builder drag(Expression drag) {
            this.drag = drag;
            return this;
        }

        public Builder forces(List<Force> forces) {
            this.forces = forces;
            return this;
        }

        public Builder force(Force force) {
            List<Force> next = new ArrayList<>(this.forces);

            next.add(force);
            this.forces = next;

            return this;
        }

        public Builder collision(@Nullable Collision collision) {
            this.collision = collision;
            return this;
        }

        public Builder parametric(@Nullable Parametric parametric) {
            this.parametric = parametric;
            return this;
        }

        public Builder size(Expression size) {
            this.size = size;
            return this;
        }

        public Builder spin(Expression spin) {
            this.spin = spin;
            return this;
        }

        public Builder initialRoll(Expression initialRoll) {
            this.initialRoll = initialRoll;
            return this;
        }

        public Builder color(Gradient color) {
            this.color = color;
            return this;
        }

        public Builder colorInterpolant(@Nullable Expression colorInterpolant) {
            this.colorInterpolant = colorInterpolant;
            return this;
        }

        public Builder colorExpressions(@Nullable List<Expression> colorExpressions) {
            this.colorExpressions = colorExpressions;
            return this;
        }

        public Builder billboard(Billboard billboard) {
            this.billboard = billboard;
            return this;
        }

        public Builder kind(DisplayKind kind) {
            this.kind = kind;
            return this;
        }

        public Builder item(String item) {
            this.item = item;
            return this;
        }

        public Builder model(@Nullable String model) {
            this.model = model;
            return this;
        }

        public Builder tint(boolean tint) {
            this.tint = tint;
            return this;
        }

        public Builder emissive(boolean emissive) {
            this.emissive = emissive;
            return this;
        }

        public Builder viewRange(double viewRange) {
            this.viewRange = viewRange;
            return this;
        }

        public ParticleSpec build() {
            return new ParticleSpec(
                    new Dynamics(
                            this.lifetime, this.speed, this.axisSpeed, this.acceleration, this.drag,
                            this.forces, this.collision, this.parametric
                    ),
                    new Look(this.size, this.spin, this.initialRoll, this.billboard, this.emissive, this.viewRange),
                    new Tinting(this.color, this.colorInterpolant, this.colorExpressions),
                    new Display(this.kind, this.item, this.model, this.tint)
            );
        }
    }
}
