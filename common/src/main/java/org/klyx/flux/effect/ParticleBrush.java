package org.klyx.flux.effect;

import org.jspecify.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Which particle a layer draws and the data that particle needs
 */
public record ParticleBrush(
        String id, double size, @Nullable Integer to,
        @Nullable String block, @Nullable String item, double speed
) {

    public static final String DUST = "minecraft:dust";
    public static final double DEFAULT_SIZE = 1;

    private static final Pattern ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");

    public ParticleBrush {
        if (!ID.matcher(id).matches()) {
            throw new IllegalArgumentException("particles id has to be written as namespace:name, e.g. minecraft:flame: \"" + id + "\"");
        }

        if (!(size > 0) || !Double.isFinite(size)) {
            throw new IllegalArgumentException("size must be positive and finite: " + size);
        }

        if (!Double.isFinite(speed)) throw new IllegalArgumentException("speed must be finite: " + speed);

        if (block != null && !ID.matcher(block).matches()) {
            throw new IllegalArgumentException("block has to be written as namespace:name, e.g. minecraft:stone: \"" + block + "\"");
        }

        if (item != null && !ID.matcher(item).matches()) {
            throw new IllegalArgumentException("item has to be written as namespace:name, e.g. minecraft:diamond: \"" + item + "\"");
        }
    }

    public static ParticleBrush dust(double size) {
        return new ParticleBrush(DUST, size, null, null, null, 0);
    }

    public static ParticleBrush of(String id) {
        return new ParticleBrush(id.contains(":") ? id : "minecraft:" + id, DEFAULT_SIZE, null, null, null, 0);
    }

    public boolean usesColor() {
        return this.id.equals(DUST)
                || this.id.equals("minecraft:dust_color_transition")
                || this.id.equals("minecraft:entity_effect");
    }
}
