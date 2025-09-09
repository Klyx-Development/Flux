package org.klyx.flux.effects;

import org.klyx.flux.utils.Vector3;

import java.util.Collection;
import java.util.function.BiConsumer;

public enum DisplayMode {
    OUTLINE(EffectFormat::generateOutline),
    SURFACE(EffectFormat::generateSurface),
    FILLED(EffectFormat::generateFilled);

    private final BiConsumer<EffectFormat, Collection<Vector3>> generator;

    DisplayMode(BiConsumer<EffectFormat, Collection<Vector3>> generator) {
        this.generator = generator;
    }

    public void generatePoints(EffectFormat format, Collection<Vector3> points) {
        generator.accept(format, points);
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
