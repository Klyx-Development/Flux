package org.klyx.flux.utils;

public class Position {
    private double x, y, z;
    private float yaw;
    private float pitch;
    private String worldName;

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
        x += dx;
        y += dy;
        z += dz;
        return this;
    }

    public Position add(Vector3 vector3) {
        x += vector3.x();
        y += vector3.y();
        z += vector3.z();
        return this;
    }

    public Position subtract(double dx, double dy, double dz) {
        x -= dx;
        y -= dy;
        z -= dz;
        return this;
    }

    @Override
    public Position clone() {
        try {
            return (Position) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
