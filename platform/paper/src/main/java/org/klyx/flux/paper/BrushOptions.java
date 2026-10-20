package org.klyx.flux.paper;

import net.minecraft.core.particles.ParticleOptions;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftParticle;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;
import org.klyx.flux.effect.Layer;
import org.klyx.flux.effect.ParticleBrush;

public final class BrushOptions {
    private final Particle particle;
    private final ParticleBrush brush;
    private final Class<?> dataType;

    private BrushOptions(Particle particle, ParticleBrush brush) {
        this.particle = particle;
        this.brush = brush;
        this.dataType = particle.getDataType();
    }

    public static BrushOptions of(ParticleBrush brush) {
        NamespacedKey key = NamespacedKey.fromString(brush.id());

        Particle particle = key == null ? null : Registry.PARTICLE_TYPE.get(key);
        if (particle == null) throw new IllegalArgumentException("there's no particle called \"" + brush.id() + "\"");

        BrushOptions options = new BrushOptions(particle, brush);
        options.create(Layer.DEFAULT_COLOR);

        return options;
    }

    public Particle particle() {
        return this.particle;
    }

    public boolean usesColor() {
        return this.brush.usesColor();
    }

    public ParticleOptions create(int rgb) {
        return CraftParticle.createParticleParam(this.particle, data(rgb & 0xFFFFFF));
    }

    private @Nullable Object data(int rgb) {
        if (this.dataType == Void.class) return null;

        if (this.dataType == Particle.DustOptions.class) {
            return new Particle.DustOptions(Color.fromRGB(rgb), (float) this.brush.size());
        }

        if (this.dataType == Particle.DustTransition.class) {
            Integer to = this.brush.to();
            return new Particle.DustTransition(
                    Color.fromRGB(rgb), Color.fromRGB(to == null ? rgb : to & 0xFFFFFF), (float) this.brush.size());
        }

        if (this.dataType == Color.class) return Color.fromRGB(rgb);
        if (this.dataType == Float.class) return (float) this.brush.size();
        if (this.dataType == Integer.class) return (int) this.brush.size();

        if (this.dataType == BlockData.class) return block();
        if (this.dataType == ItemStack.class) return item();

        throw new IllegalArgumentException("\"" + this.brush.id() + "\" needs " + this.dataType.getSimpleName());
    }

    private BlockData block() {
        String block = this.brush.block();
        if (block == null) throw new IllegalArgumentException("\"" + this.brush.id() + "\" needs a \"block\"");

        try {
            return Bukkit.createBlockData(block);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("there is no block called \"" + block + "\"", e);
        }
    }

    private ItemStack item() {
        String item = this.brush.item();
        if (item == null) throw new IllegalArgumentException("\"" + this.brush.id() + "\" needs an \"item\"");

        NamespacedKey key = NamespacedKey.fromString(item);
        Material material = key == null ? null : Registry.MATERIAL.get(key);
        if (material == null || !material.isItem()) throw new IllegalArgumentException("there's no item called \"" + item + "\"");

        return new ItemStack(material);
    }
}
