package org.klyx.flux.effects;

import org.jetbrains.annotations.NotNull;
import org.klyx.flux.utils.Vector3;

import java.util.Collection;
import java.util.UUID;

public interface EffectFormat extends Cloneable {

    Collection<Vector3> getPoints();
    /**
     * Forces regeneration of points on next access.
     */
    void invalidatePoints();

    void generatePoints(DisplayMode mode, Collection<Vector3> points);

    default void generateOutline(Collection<Vector3> points) {}

    default void generateSurface(Collection<Vector3> points) {}

    default void generateFilled(Collection<Vector3> points) {}

    String getEffectType();
    UUID getEffectId();
    boolean getDirty();
    void setDirty(boolean dirty);

    EffectFormat clone();

}
