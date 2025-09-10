package org.klyx.flux.utils;

public class Vector3 {
    private double x, y, z;

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3() {
        this(0, 0, 0);
    }

    public Vector3(Vector3 otherVector) {
        this(otherVector.x(), otherVector.y(), otherVector.z());
    }

    public double x() { return x; }
    public double y() { return y; }
    public double z() { return z; }

    public Vector3 setX(double x) {
        this.x = x;
        return this;
    }

    public Vector3 setY(double y) {
        this.y = y;
        return this;
    }

    public Vector3 setZ(double z) {
        this.z = z;
        return this;
    }

    public Vector3 add(Vector3 otherVector) {
        x += otherVector.x();
        y += otherVector.y();
        z += otherVector.z();
        return this;
    }

    public Vector3 subtract(Vector3 otherVector) {
        x -= otherVector.x();
        y -= otherVector.y();
        z -= otherVector.z();
        return this;
    }

    public Vector3 multiply(double scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        return this;
    }

    public Vector3 divide(double scalar) {
        x /= scalar;
        y /= scalar;
        z /= scalar;
        return this;
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double lengthSquared() {
        return x * x + y * y + z * z;
    }

    public double distance(Vector3 otherVector) {
        return subtract(otherVector).length();
    }

    public Vector3 normalize() {
        double length = length();
        return new Vector3(x / length, y / length, z / length);
    }

    public double dotProduct(Vector3 otherVector) {
        return x * otherVector.x() + y * otherVector.y() + z * otherVector.z();
    }

    public Vector3 crossProduct(Vector3 otherVector) {
        return new Vector3(
            y * otherVector.z() - z * otherVector.y(),
            z * otherVector.x() - x * otherVector.z(),
            x * otherVector.y() - y * otherVector.x()
        );
    }

    public double angleBetween(Vector3 otherVector) {
        double dotProduct = dotProduct(otherVector);
        double lengthProduct = length() * otherVector.length();
        return Math.acos(dotProduct / lengthProduct);
    }

    @Override
    public Vector3 clone() {
        return new Vector3(this);
    }
}
