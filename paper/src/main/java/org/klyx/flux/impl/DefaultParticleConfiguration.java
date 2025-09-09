package org.klyx.flux.impl;

import org.jetbrains.annotations.Nullable;
import org.klyx.flux.data.particle.ParticleConfiguration;
import org.klyx.flux.data.particle.ParticleType;

public class DefaultParticleConfiguration implements ParticleConfiguration {

    private ParticleType particleType = ParticleType.ASH;
    private @Nullable Object particleData = null;
    private int count = 1;
    private double offsetX = 0;
    private double offsetY = 0;
    private double offsetZ = 0;
    private double extra = 0;

    @Override
    public ParticleType getParticleType() {
        return particleType;
    }

    @Override
    public void setParticleType(ParticleType type) {
        this.particleType = type;
    }

    @Override
    public @Nullable Object getParticleData() {
        return particleData;
    }

    @Override
    public void setParticleData(@Nullable Object data) {
        this.particleData = data;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public void setCount(int count) {
        this.count = Math.max(0, count);
    }

    @Override
    public double getOffsetX() {
        return offsetX;
    }

    @Override
    public void setOffsetX(double offset) {
        this.offsetX = offset;
    }

    @Override
    public double getOffsetY() {
        return offsetY;
    }

    @Override
    public void setOffsetY(double offset) {
        this.offsetY = offset;
    }

    @Override
    public double getOffsetZ() {
        return offsetZ;
    }

    @Override
    public void setOffsetZ(double offset) {
        this.offsetZ = offset;
    }

    @Override
    public double getExtra() {
        return extra;
    }

    @Override
    public void setExtra(double speed) {
        this.extra = speed;
    }

}
