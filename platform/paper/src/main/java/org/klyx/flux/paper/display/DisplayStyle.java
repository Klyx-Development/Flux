package org.klyx.flux.paper.display;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.klyx.exo.entity.data.ExoEntityType;
import org.klyx.exo.entity.data.ExoItemStack;
import org.klyx.exo.entity.meta.BillboardConstraints;
import org.klyx.exo.util.Key;
import org.klyx.flux.sim.spec.Billboard;
import org.klyx.flux.sim.spec.DisplayKind;
import org.klyx.flux.sim.spec.ParticleSpec;

public final class DisplayStyle {
    private static final Key DYED_COLOR = Key.of("minecraft", "dyed_color");
    private static final Key ITEM_MODEL = Key.of("minecraft", "item_model");
    private static final Key FIREWORK_EXPLOSION = Key.of("minecraft", "firework_explosion");
    private static final String FIREWORK_STAR = "minecraft:firework_star";

    private final DisplayKind kind;
    private final BillboardConstraints billboard;
    private final boolean emissive;
    private final float viewRange;
    private final int glideTicks;
    private final String item;
    private final @Nullable String model;
    private final boolean tint;
    private final boolean fireworkTint;
    private final ExoEntityType entityType;

    public DisplayStyle(ParticleSpec particle, int glideTicks) {
        this.kind = particle.kind();
        this.billboard = convert(particle.billboard());
        this.emissive = particle.emissive();
        this.viewRange = (float) particle.viewRange();
        this.glideTicks = glideTicks;
        this.item = particle.item();
        this.model = particle.model();
        this.tint = particle.tint();
        this.fireworkTint = FIREWORK_STAR.equals(particle.item());
        this.entityType = new ExoEntityType(Key.of(this.kind == DisplayKind.TEXT ? "text_display" : "item_display"));

        if (this.kind == DisplayKind.ITEM) {
            Identifier id = Identifier.tryParse(this.item);

            if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
                throw new IllegalArgumentException("there's no item called \"" + this.item + "\"");
            }
        }
    }

    public DisplayKind kind() {
        return this.kind;
    }

    public BillboardConstraints billboard() {
        return this.billboard;
    }

    public boolean emissive() {
        return this.emissive;
    }

    public float viewRange() {
        return this.viewRange;
    }

    public int glideTicks() {
        return this.glideTicks;
    }

    public ExoEntityType entityType() {
        return this.entityType;
    }

    public boolean usesColor() {
        return this.kind == DisplayKind.TEXT || this.tint;
    }

    public ExoItemStack stack(int rgb) {
        ExoItemStack.Builder builder = ExoItemStack.builder(Key.of(this.item));
        if (this.model != null) builder.rawComponent(ITEM_MODEL, StringBinaryTag.stringBinaryTag(this.model));

        if (this.tint) {
            if (this.fireworkTint) {
                builder.rawComponent(FIREWORK_EXPLOSION, fireworkExplosion(rgb));
            } else {
                builder.rawComponent(DYED_COLOR, IntBinaryTag.intBinaryTag(rgb & 0xFFFFFF));
            }
        }

        return builder.build();
    }

    private static CompoundBinaryTag fireworkExplosion(int rgb) {
        return CompoundBinaryTag.builder()
                .putString("shape", "small_ball")
                .put("colors", IntArrayBinaryTag.intArrayBinaryTag(rgb & 0xFFFFFF))
                .build();
    }

    private static BillboardConstraints convert(Billboard billboard) {
        return switch (billboard) {
            case FIXED -> BillboardConstraints.FIXED;
            case VERTICAL -> BillboardConstraints.VERTICAL;
            case HORIZONTAL -> BillboardConstraints.HORIZONTAL;
            case CENTER -> BillboardConstraints.CENTER;
        };
    }
}
