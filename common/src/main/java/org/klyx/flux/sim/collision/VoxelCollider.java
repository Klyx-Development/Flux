package org.klyx.flux.sim.collision;

public final class VoxelCollider implements Collider {
    @FunctionalInterface
    public interface Blocks {
        boolean isSolid(int x, int y, int z);
    }

    private final Blocks blocks;

    public VoxelCollider(Blocks blocks) {
        this.blocks = blocks;
    }

    @Override
    public boolean sweep(double x, double y, double z, double dx, double dy, double dz, double radius, Hit hit) {
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (length < 1.0e-9) return false;

        int cubeX = (int) Math.floor(x);
        int cubeY = (int) Math.floor(y);
        int cubeZ = (int) Math.floor(z);

        if (this.blocks.isSolid(cubeX, cubeY, cubeZ)) return false;

        double unitX = dx / length;
        double unitY = dy / length;
        double unitZ = dz / length;

        int stepX = unitX > 0 ? 1 : -1;
        int stepY = unitY > 0 ? 1 : -1;
        int stepZ = unitZ > 0 ? 1 : -1;

        double deltaX = unitX == 0 ? Double.POSITIVE_INFINITY : Math.abs(1 / unitX);
        double deltaY = unitY == 0 ? Double.POSITIVE_INFINITY : Math.abs(1 / unitY);
        double deltaZ = unitZ == 0 ? Double.POSITIVE_INFINITY : Math.abs(1 / unitZ);

        double nextX = unitX == 0 ? Double.POSITIVE_INFINITY : (unitX > 0 ? cubeX + 1 - x : x - cubeX) / Math.abs(unitX);
        double nextY = unitY == 0 ? Double.POSITIVE_INFINITY : (unitY > 0 ? cubeY + 1 - y : y - cubeY) / Math.abs(unitY);
        double nextZ = unitZ == 0 ? Double.POSITIVE_INFINITY : (unitZ > 0 ? cubeZ + 1 - z : z - cubeZ) / Math.abs(unitZ);

        double reach = length + radius;

        for (int i = 0; i < 256; i++) {
            double entry;

            if (nextX <= nextY && nextX <= nextZ) {
                entry = nextX;
                nextX += deltaX;
                cubeX += stepX;

                if (entry > reach) return false;
                if (this.blocks.isSolid(cubeX, cubeY, cubeZ)) return stop(hit, entry, radius, length, -stepX, 0, 0);
            } else if (nextY <= nextZ) {
                entry = nextY;
                nextY += deltaY;
                cubeY += stepY;

                if (entry > reach) return false;
                if (this.blocks.isSolid(cubeX, cubeY, cubeZ)) return stop(hit, entry, radius, length, 0, -stepY, 0);
            } else {
                entry = nextZ;
                nextZ += deltaZ;
                cubeZ += stepZ;

                if (entry > reach) return false;
                if (this.blocks.isSolid(cubeX, cubeY, cubeZ)) return stop(hit, entry, radius, length, 0, 0, -stepZ);
            }
        }

        return false;
    }

    private static boolean stop(Hit hit, double entry, double radius, double length, int nx, int ny, int nz) {
        hit.t = Math.clamp((entry - radius) / length, 0, 1);
        hit.nx = nx;
        hit.ny = ny;
        hit.nz = nz;

        return true;
    }
}
