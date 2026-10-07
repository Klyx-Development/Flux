package org.klyx.flux.effect.playback;

public interface Playback {
    void pause();

    void resume();

    void stop();

    boolean isPaused();

    boolean isStopped();
}
