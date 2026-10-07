package org.klyx.flux.effect.anchor;

import org.klyx.flux.math.Vec3;

public record Bounds(double width, double height, double centerY) {
    public static final Bounds NONE = new Bounds(0, 0, 0);

    public Bounds {
        if (!(width >= 0) || !Double.isFinite(width)) {
            throw new IllegalArgumentException("width has to be greater than or equal to 0 and finite: " + width);
        }

        if (!(height >= 0) || !Double.isFinite(height)) {
            throw new IllegalArgumentException("height has to be greater than or equal to 0 and finite: " + height);
        }

        if (!Double.isFinite(centerY)) throw new IllegalArgumentException("centerY must be finite: " + centerY);
    }

    public boolean isEmpty() {
        return !(this.width > 0) || !(this.height > 0);
    }

    public Bounds grown(double margin) {
        return new Bounds(this.width + 2 * margin, this.height + 2 * margin, this.centerY);
    }

    public Vec3 size() {
        return new Vec3(this.width, this.height, this.width);
    }

    public Vec3 center() {
        return new Vec3(0, this.centerY, 0);
    }

    public double longest() {
        return Math.max(this.width, this.height);
    }
}
