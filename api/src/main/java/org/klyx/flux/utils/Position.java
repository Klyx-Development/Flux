package org.klyx.flux.utils;

public class Position {
    private final double x, y, z;
    private final float yaw;
    private final float pitch;
    private final String worldName;

    public Position(double x, double y, double z, float yaw, float pitch, String worldName) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.worldName = worldName;
    }

    public Position(double x, double y, double z, String worldName) {
        this(x, y, z, 0, 0, worldName);
    }

    public double x() { return x; }
    public double y() { return y; }
    public double z() { return z; }
    public float yaw() { return yaw; }
    public float pitch() { return pitch; }
    public String worldName() { return worldName; }

    public Position add(double dx, double dy, double dz) {
        return new Position(x + dx, y + dy, z + dz, worldName);
    }

    public Position subtract(double dx, double dy, double dz) {
        return new Position(x - dx, y - dy, z - dz, worldName);
    }

}
