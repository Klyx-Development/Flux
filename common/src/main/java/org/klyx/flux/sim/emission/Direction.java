package org.klyx.flux.sim.emission;

import org.klyx.flux.expr.Expression;
import org.klyx.flux.expr.Variables;
import org.klyx.flux.math.Vec3;

import java.util.List;
import java.util.random.RandomGenerator;

public sealed interface Direction
        permits Direction.Computed, Direction.Cone, Direction.Fixed, Direction.Inwards, Direction.None, Direction.Outwards {

    void resolve(RandomGenerator random, double[] offset, Variables variables, double[] out);

    default void resolve(RandomGenerator random, double[] offset, double[] out) {
        resolve(random, offset, Variables.NONE, out);
    }

    record Outwards() implements Direction {
        @Override
        public void resolve(RandomGenerator random, double[] offset, Variables variables, double[] out) {
            fromOffset(random, offset, out, 1);
        }
    }

    record Inwards() implements Direction {
        @Override
        public void resolve(RandomGenerator random, double[] offset, Variables variables, double[] out) {
            fromOffset(random, offset, out, -1);
        }
    }

    record None() implements Direction {
        @Override
        public void resolve(RandomGenerator random, double[] offset, Variables variables, double[] out) {
            out[0] = 0;
            out[1] = 0;
            out[2] = 0;
        }
    }

    record Fixed(Vec3 direction) implements Direction {
        public Fixed {
            Vec3 unit = direction.normalize();

            if (unit.equals(Vec3.ZERO)) throw new IllegalArgumentException("a fixed direction can't be zero");

            direction = unit;
        }

        @Override
        public void resolve(RandomGenerator random, double[] offset, Variables variables, double[] out) {
            out[0] = this.direction.x();
            out[1] = this.direction.y();
            out[2] = this.direction.z();
        }
    }

    record Computed(List<Expression> components) implements Direction {
        public Computed {
            if (components.size() != 3) {
                throw new IllegalArgumentException("a computed direction needs 3 expressions but has " + components.size());
            }

            components = List.copyOf(components);
        }

        @Override
        public void resolve(RandomGenerator random, double[] offset, Variables variables, double[] out) {
            double x = finite(this.components.get(0).eval(variables));
            double y = finite(this.components.get(1).eval(variables));
            double z = finite(this.components.get(2).eval(variables));
            double length = Math.sqrt(x * x + y * y + z * z);

            if (!(length > 1.0e-12) || !Double.isFinite(length)) {
                out[0] = 0;
                out[1] = 0;
                out[2] = 0;
                return;
            }

            out[0] = x / length;
            out[1] = y / length;
            out[2] = z / length;
        }

        private static double finite(double value) {
            return Double.isFinite(value) ? value : 0;
        }
    }

    record Cone(Vec3 axis, double halfAngleDegrees) implements Direction {
        public Cone {
            Vec3 unit = axis.normalize();

            if (unit.equals(Vec3.ZERO)) throw new IllegalArgumentException("a cone's axis can't be zero");
            if (!(halfAngleDegrees >= 0 && halfAngleDegrees <= 180)) {
                throw new IllegalArgumentException("a cone's angle has to be between 0 and 180 degrees: " + halfAngleDegrees);
            }

            axis = unit;
        }

        @Override
        public void resolve(RandomGenerator random, double[] offset, Variables variables, double[] out) {
            double cosMax = Math.cos(Math.toRadians(this.halfAngleDegrees));
            double cosTheta = cosMax + random.nextDouble() * (1 - cosMax);
            double sinTheta = Math.sqrt(Math.max(0, 1 - cosTheta * cosTheta));
            double phi = random.nextDouble() * 2 * Math.PI;

            Vec3 helper = Math.abs(this.axis.y()) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
            Vec3 first = this.axis.cross(helper).normalize();
            Vec3 second = this.axis.cross(first);
            double firstScale = Math.cos(phi) * sinTheta;
            double secondScale = Math.sin(phi) * sinTheta;

            out[0] = this.axis.x() * cosTheta + first.x() * firstScale + second.x() * secondScale;
            out[1] = this.axis.y() * cosTheta + first.y() * firstScale + second.y() * secondScale;
            out[2] = this.axis.z() * cosTheta + first.z() * firstScale + second.z() * secondScale;
        }
    }

    private static void fromOffset(RandomGenerator random, double[] offset, double[] out, int sign) {
        double length = Math.sqrt(offset[0] * offset[0] + offset[1] * offset[1] + offset[2] * offset[2]);

        if (length < 1.0e-9) {
            EmitterShape.randomUnit(random, out);
            return;
        }

        out[0] = sign * offset[0] / length;
        out[1] = sign * offset[1] / length;
        out[2] = sign * offset[2] / length;
    }
}
