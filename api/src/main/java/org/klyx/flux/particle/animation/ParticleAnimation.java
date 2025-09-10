package org.klyx.flux.particle.animation;

import org.klyx.flux.utils.player.FluxPlayer;
import org.klyx.flux.utils.Position;

import java.util.Collection;
import java.util.UUID;

public interface ParticleAnimation {

    UUID getAnimationId();
    UUID getEffectId();
    Position getPosition();

    boolean isRunning();
    boolean isPaused();

    void start();
    void pause();
    void resume();
    void stop();

    long getElapsedTime();
    double getProgress(); // 0.0 -> 1.0

    AnimationSettings getSettings();
    Collection<FluxPlayer> getViewers();

}
