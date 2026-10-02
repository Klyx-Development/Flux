package org.klyx.flux.sim.emission;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.klyx.flux.math.Vec3;

import java.util.random.RandomGenerator;

public sealed interface EmitterShape permits EmitterShape.Box, EmitterShape.Disc, EmitterShape.Point, EmitterShape.Sphere {
    void sample(RandomGenerator random, double[] out);

    record Point() implements EmitterShape {
        @Override
        public void sample(RandomGenerator random, double[] out) {
            out[0] = 0;
            out[1] = 0;
            out[2] = 0;
        }
    }

    record Sphere(double radius, boolean surfaceOnly) implements EmitterShape {
        public Sphere {
            requireSize("radius", radius);
        }

        @Override
        public void sample(RandomGenerator random, double[] out) {
            randomUnit(random, out);

            double length = this.surfaceOnly ? this.radius : this.radius * Math.cbrt(random.nextDouble());

            out[0] *= length;
            out[1] *= length;
            out[2] *= length;
        }
    }

    record Box(Vec3 halfSize, boolean surfaceOnly) implements EmitterShape {
        public Box {
            requireSize("halfSize.x", halfSize.x());
            requireSize("halfSize.y", halfSize.y());
            requireSize("halfSize.z", halfSize.z());
        }

        @Override
        public void sample(RandomGenerator random, double[] out) {
            double halfX = this.halfSize.x();
            double halfY = this.halfSize.y();
            double halfZ = this.halfSize.z();

            out[0] = (random.nextDouble() * 2 - 1) * halfX;
            out[1] = (random.nextDouble() * 2 - 1) * halfY;
            out[2] = (random.nextDouble() * 2 - 1) * halfZ;

            if (!this.surfaceOnly) return;

            double areaX = halfY * halfZ;
            double areaY = halfX * halfZ;
            double areaZ = halfX * halfY;
            double total = areaX + areaY + areaZ;

            if (total <= 0) return;

            double pick = random.nextDouble() * total;
            int axis = pick < areaX ? 0 : pick < areaX + areaY ? 1 : 2;
            double extent = axis == 0 ? halfX : axis == 1 ? halfY : halfZ;

            out[axis] = random.nextBoolean() ? extent : -extent;
        }
    }

    record Disc(double radius, boolean surfaceOnly, Vec3 normal) implements EmitterShape {
        public Disc {
            requireSize("radius", radius);

            Vec3 unit = normal.normalize();
            if (unit.equals(Vec3.ZERO)) throw new IllegalArgumentException("a disc's normal can't be zero");

            normal = unit;
        }

        public Disc(double radius, boolean surfaceOnly) {
            this(radius, surfaceOnly, new Vec3(0, 1, 0));
        }

        @Override
        public void sample(RandomGenerator random, double[] out) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double length = this.surfaceOnly ? this.radius : this.radius * Math.sqrt(random.nextDouble());
            double x = Math.cos(angle) * length;
            double z = Math.sin(angle) * length;

            if (this.normal.x() == 0 && this.normal.z() == 0 && this.normal.y() > 0) {
                out[0] = x;
                out[1] = 0;
                out[2] = z;
                return;
            }

            Quaterniond turn = new Quaterniond().rotationTo(
                    new Vector3d(0, 1, 0),
                    new Vector3d(this.normal.x(), this.normal.y(), this.normal.z())
            );
            Vector3d point = turn.transform(new Vector3d(x, 0, z));

            out[0] = point.x;
            out[1] = point.y;
            out[2] = point.z;
        }
    }

    static void randomUnit(RandomGenerator random, double[] out) {
        double y = random.nextDouble() * 2 - 1;
        double angle = random.nextDouble() * 2 * Math.PI;
        double ring = Math.sqrt(Math.max(0, 1 - y * y));

        out[0] = Math.cos(angle) * ring;
        out[1] = y;
        out[2] = Math.sin(angle) * ring;
    }

    private static void requireSize(String name, double value) {
        if (!(value >= 0) || !Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " has be greater than or equal to zero and finite: " + value);
        }
    }
}
