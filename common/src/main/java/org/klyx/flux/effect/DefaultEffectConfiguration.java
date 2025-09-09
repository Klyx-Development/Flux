package org.klyx.flux.effect;

import org.klyx.flux.utils.Vector3;
import org.klyx.flux.effects.DisplayMode;
import org.klyx.flux.effects.EffectConfiguration;

public class DefaultEffectConfiguration implements EffectConfiguration {

    private DisplayMode displayMode = DisplayMode.SURFACE;
    private Vector3 rotation = new Vector3();
    private double scale = 1.0;
    private Vector3 offset = new Vector3();
    private double particleSpacing = 0.2;
    private int targetParticleCount = 100;
    private int maxParticleCount = 2000;

    public DefaultEffectConfiguration() {}

    public DefaultEffectConfiguration(EffectConfiguration configuration) {
        this.displayMode = configuration.getDisplayMode();
        this.rotation = configuration.getRotation();
        this.scale = configuration.getScale();
        this.offset = configuration.getOffset();
        this.particleSpacing = configuration.getParticleSpacing();
        this.targetParticleCount = configuration.getTargetParticleCount();
        this.maxParticleCount = configuration.getMaxParticleCount();
    }

    @Override
    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    @Override
    public void setDisplayMode(DisplayMode mode) {
        this.displayMode = mode;
    }

    @Override
    public Vector3 getRotation() {
        return rotation;
    }

    @Override
    public void setRotation(Vector3 rotation) {
        this.rotation = rotation;
    }

    @Override
    public void rotate(double x, double y, double z) {
        rotation = rotation.add(new Vector3(x, y, z));
    }

    @Override
    public double getScale() {
        return scale;
    }

    @Override
    public void setScale(double scale) {
        this.scale = Math.max(0.01, scale);
    }

    @Override
    public Vector3 getOffset() {
        return offset.clone();
    }

    @Override
    public void setOffset(Vector3 offset) {
        this.offset = offset.clone();
    }

    @Override
    public double getParticleSpacing() {
        return particleSpacing;
    }

    @Override
    public void setParticleSpacing(double spacing) {
        this.particleSpacing = spacing;
    }

    @Override
    public int getTargetParticleCount() {
        return targetParticleCount;
    }

    @Override
    public void setTargetParticleCount(int count) {
        this.targetParticleCount = count;
    }

    @Override
    public int getMaxParticleCount() {
        return maxParticleCount;
    }

    @Override
    public void setMaxParticleCount(int maxCount) {
        this.maxParticleCount = maxCount;
    }
}
