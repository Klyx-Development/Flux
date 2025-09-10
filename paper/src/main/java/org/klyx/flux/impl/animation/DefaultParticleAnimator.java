package org.klyx.flux.impl.animation;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.klyx.flux.effects.EffectFormat;
import org.klyx.flux.particle.ParticleRenderer;
import org.klyx.flux.particle.animation.AnimationSettings;
import org.klyx.flux.particle.animation.ParticleAnimation;
import org.klyx.flux.particle.animation.ParticleAnimator;
import org.klyx.flux.utils.Position;
import org.klyx.flux.utils.Vector3;
import org.klyx.flux.utils.player.FluxPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultParticleAnimator implements ParticleAnimator {

    private final Plugin plugin;
    private final ParticleRenderer renderer;
    private final Map<UUID, ParticleAnimationImpl> activeAnimations = new ConcurrentHashMap<>();
    private final AtomicLong animationId = new AtomicLong(0);

    public DefaultParticleAnimator(Plugin plugin, ParticleRenderer renderer) {
        this.plugin = plugin;
        this.renderer = renderer;
    }

    @Override
    public ParticleAnimation createAnimation(EffectFormat format, AnimationSettings settings) {
        UUID animId = UUID.randomUUID();
        return new ParticleAnimationImpl(animId, format, settings, plugin, renderer);
    }

    @Override
    public ParticleAnimation startAnimation(EffectFormat format, Position position, Collection<FluxPlayer> viewers, AnimationSettings settings) {
        ParticleAnimationImpl animation = new ParticleAnimationImpl(
                UUID.randomUUID(), format, settings, plugin, renderer
        );

        animation.setPosition(position);
        animation.setViewers(new ArrayList<>(viewers));

        activeAnimations.put(animation.getAnimationId(), animation);
        animation.start();

        return animation;
    }

    @Override
    public void stopAnimations(UUID effectId) {
        activeAnimations.values().removeIf(animation -> {
            if (animation.getEffectId().equals(effectId)) {
                animation.stop();
                return true;
            }
            return false;
        });
    }

    @Override
    public Collection<ParticleAnimation> getActiveAnimations() {
        return new ArrayList<>(activeAnimations.values());
    }

    private class ParticleAnimationImpl implements ParticleAnimation {

        private final UUID animationId;
        private final EffectFormat effect;
        private final AnimationSettings settings;
        private final Plugin plugin;
        private final ParticleRenderer renderer;

        private Position position;
        private Collection<FluxPlayer> viewers = new ArrayList<>();
        private boolean running = false;
        private boolean paused = false;
        private long startTime;
        private long pausedTime = 0;
        private BukkitTask task;

        public ParticleAnimationImpl(UUID animationId, EffectFormat effect, AnimationSettings settings,
                                     Plugin plugin, ParticleRenderer renderer) {
            this.animationId = animationId;
            this.effect = effect;
            this.settings = settings;
            this.plugin = plugin;
            this.renderer = renderer;
        }

        @Override
        public void start() {
            if (running) return;

            running = true;
            paused = false;
            startTime = System.currentTimeMillis();

            Collection<Vector3> allPoints = new ArrayList<>(effect.getPoints());
            List<Vector3> pointsList = new ArrayList<>(allPoints);

            task = new BukkitRunnable() {
                private int currentFrame = 0;

                @Override
                public void run() {
                    if (paused) return;

                    if (getElapsedTime() >= settings.getDurationMs()) {
                        if (settings.isLoop()) {
                            startTime = System.currentTimeMillis();
                            currentFrame = 0;
                        } else {
                            stop();
                            return;
                        }
                    }

                    int particlesPerFrame = settings.getParticlesPerFrame();
                    int startIndex = currentFrame * particlesPerFrame;
                    int endIndex = Math.min(startIndex + particlesPerFrame, pointsList.size());

                    if (startIndex < pointsList.size()) {
                        List<Vector3> framePoints = pointsList.subList(startIndex, endIndex);
                    }

                    currentFrame++;
                }
            }.runTaskTimer(plugin, 0, settings.getDelayTicks());

        }

        @Override
        public void stop() {
            running = false;
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
            activeAnimations.remove(animationId);
        }

        @Override
        public void pause() {
            paused = true;
            pausedTime = System.currentTimeMillis();
        }

        @Override
        public void resume() {
            paused = false;
        }

        @Override
        public UUID getAnimationId() {
            return animationId;
        }

        @Override
        public UUID getEffectId() {
            return effect.getEffectId();
        }

        @Override
        public Position getPosition() {
            return position;
        }

        public void setPosition(Position position) {
            this.position = position;
        }

        @Override
        public boolean isRunning() {
            return running;
        }

        @Override
        public boolean isPaused() {
            return paused;
        }

        @Override
        public long getElapsedTime() {
            if (!running) return 0;
            return System.currentTimeMillis() - startTime - pausedTime;
        }

        @Override
        public double getProgress() {
            if (!running) return 0.0;
            return Math.min(1.0, (double) getElapsedTime() / settings.getDurationMs());
        }

        @Override
        public AnimationSettings getSettings() {
            return settings;
        }

        @Override
        public Collection<FluxPlayer> getViewers() {
            return new ArrayList<>(viewers);
        }

        public void setViewers(Collection<FluxPlayer> viewers) {
            this.viewers = viewers;
        }
    }
}
