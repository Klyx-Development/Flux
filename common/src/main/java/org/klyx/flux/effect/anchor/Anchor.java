package org.klyx.flux.effect.anchor;

import org.klyx.flux.math.Vec3;

// read from the effect's own thread, so implementations must be thread-safe!
public interface Anchor {
    Vec3 position();

    default boolean isValid() {
        return true;
    }

    default Bounds bounds() {
        return Bounds.NONE;
    }

    static Anchor fixed(Vec3 position) {
        return () -> position;
    }
}
