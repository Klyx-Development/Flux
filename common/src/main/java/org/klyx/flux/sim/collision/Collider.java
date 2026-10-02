package org.klyx.flux.sim.collision;

@FunctionalInterface
public interface Collider {
    Collider NONE = (_, _, _, _, _, _, _, _) -> false;

    boolean sweep(double x, double y, double z, double dx, double dy, double dz, double radius, Hit hit);

    static Collider floor(double y) {
        return (x, py, z, dx, dy, dz, radius, hit) -> {
            if (py < y || dy >= 0) return false;

            double stop = y + radius;

            if (py + dy >= stop) return false;

            hit.t = Math.clamp((py - stop) / -dy, 0, 1);
            hit.nx = 0;
            hit.ny = 1;
            hit.nz = 0;

            return true;
        };
    }

    final class Hit {
        public double t;
        public double nx;
        public double ny;
        public double nz;
    }
}
