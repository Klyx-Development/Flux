package org.klyx.flux.paper.display.simulation;

import org.klyx.flux.paper.display.DisplayEmitter;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Runs every emitter on an async thread
 */
public final class SimulationRunner {

    private final Logger logger;
    private final CopyOnWriteArrayList<DisplayEmitter> emitters = new CopyOnWriteArrayList<>();

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "flux-sim");
        thread.setDaemon(true);
        return thread;
    });

    private long lastTick = System.nanoTime();

    public SimulationRunner(Logger logger) {
        this.logger = logger;
        this.executor.scheduleAtFixedRate(this::tick, 50, 50, TimeUnit.MILLISECONDS);
    }

    public void add(DisplayEmitter emitter) {
        this.emitters.add(emitter);
    }

    public boolean isEmpty() {
        return this.emitters.isEmpty();
    }

    public int size() {
        return this.emitters.size();
    }

    public void stopAll() {
        for (DisplayEmitter emitter : this.emitters) {
            emitter.stop();
        }
    }

    public void shutdown() {
        this.executor.shutdownNow();

        for (DisplayEmitter emitter : this.emitters) {
            emitter.stop();
            emitter.tick(0);
        }

        this.emitters.clear();
    }

    public Iterable<Collection<UUID>> watchers() {
        return () -> {
            Iterator<DisplayEmitter> inner = this.emitters.iterator();

            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return inner.hasNext();
                }

                @Override
                public Collection<UUID> next() {
                    return inner.next().watching();
                }
            };
        };
    }

    private void tick() {
        // an exception escaping a scheduled task would cancel every later run,
        // so just log rather than throwing
        try {
            long now = System.nanoTime();
            double deltaSeconds =
                    Math.min((now - this.lastTick) / (double) TimeUnit.SECONDS.toNanos(1), 0.25);
            this.lastTick = now;

            for (DisplayEmitter emitter : this.emitters) {
                tickOne(emitter, deltaSeconds);
            }

        } catch (RuntimeException e) {
            this.logger.error("display particle tick failed: ", e);
        }
    }

    private void tickOne(DisplayEmitter emitter, double deltaSeconds) {
        try {
            if (!emitter.tick(deltaSeconds)) {
                this.emitters.remove(emitter);
            }
        } catch (RuntimeException e) {
            this.logger.error("display emitter failed, stopping: ", e);
            emitter.stop();
            this.emitters.remove(emitter);
        }
    }
}
