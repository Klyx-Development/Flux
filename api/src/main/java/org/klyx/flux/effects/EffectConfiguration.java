package org.klyx.flux.effects;

import org.klyx.flux.utils.Vector3;

public interface EffectConfiguration {
    DisplayMode getDisplayMode();
    void setDisplayMode(DisplayMode mode);

    Vector3 getRotation();
    void setRotation(Vector3 rotation);
    void rotate(double x, double y, double z);

    double getScale();
    void setScale(double scale);

    Vector3 getOffset();
    void setOffset(Vector3 offset);

    double getParticleSpacing();
    void setParticleSpacing(double spacing);

    int getTargetParticleCount();
    void setTargetParticleCount(int count);

    int getMaxParticleCount();
    void setMaxParticleCount(int maxCount);

}
