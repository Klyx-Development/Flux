package org.klyx.flux.data.particle.animation;

public class AnimationSettings {
    private long durationMs = 1000;
    private int delayTicks = 2;
    private int particlesPerFrame = 10;
    private boolean loop = false;
    private AnimationType type = AnimationType.LINEAR;

    public long getDurationMs() {
        return durationMs;
    }

    public AnimationSettings setDurationMs(long durationMs) {
        this.durationMs = durationMs;
        return this;
    }

    public int getDelayTicks() {
        return delayTicks;
    }

    public AnimationSettings setDelayTicks(int delayTicks) {
        this.delayTicks = delayTicks;
        return this;
    }

    public int getParticlesPerFrame() {
        return particlesPerFrame;
    }

    public AnimationSettings setParticlesPerFrame(int particlesPerFrame) {
        this.particlesPerFrame = particlesPerFrame;
        return this;
    }

    public boolean isLoop() {
        return loop;
    }

    public AnimationSettings setLoop(boolean loop) {
        this.loop = loop;
        return this;
    }

    public AnimationType getType() {
        return type;
    }

    public AnimationSettings setType(AnimationType type) {
        this.type = type;
        return this;
    }
}
