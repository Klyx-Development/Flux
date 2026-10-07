package org.klyx.flux.effect.anchor;

import org.klyx.flux.math.Vec3;

public class DynamicAnchor implements Anchor {
    private volatile Vec3 position;
    private volatile Bounds bounds = Bounds.NONE;
    private volatile boolean valid = true;

    public DynamicAnchor(Vec3 position) {
        this.position = position;
    }

    public void update(Vec3 position) {
        this.position = position;
    }

    public void update(double x, double y, double z) {
        Vec3 current = this.position;
        if (current.x() == x && current.y() == y && current.z() == z) return;

        this.position = new Vec3(x, y, z);
    }

    public void updateBounds(double width, double height, double centerY) {
        Bounds current = this.bounds;
        if (current.width() == width && current.height() == height && current.centerY() == centerY) return;

        this.bounds = new Bounds(width, height, centerY);
    }

    public void invalidate() {
        this.valid = false;
    }

    @Override
    public Vec3 position() {
        return this.position;
    }

    @Override
    public Bounds bounds() {
        return this.bounds;
    }

    @Override
    public boolean isValid() {
        return this.valid;
    }
}
