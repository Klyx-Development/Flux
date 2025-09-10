package org.klyx.flux.particle;

import org.jetbrains.annotations.Nullable;

public interface ParticleConfiguration {

    ParticleType getParticleType();
    void setParticleType(ParticleType type);

    @Nullable Object getParticleData();
    void setParticleData(@Nullable Object data);

    int getCount();
    void setCount(int count);

    double getOffsetX();
    void setOffsetX(double offset);

    double getOffsetY();
    void setOffsetY(double offset);

    double getOffsetZ();
    void setOffsetZ(double offset);

    double getExtra();
    void setExtra(double speed);

}
