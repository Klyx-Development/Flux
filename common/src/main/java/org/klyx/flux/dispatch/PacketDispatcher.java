package org.klyx.flux.dispatch;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class PacketDispatcher<P> {

    private final PacketSender<P> sender;
    private final TickScheduler scheduler;
    private final Limits limits;
    private final ExecutorService executor;

    private final ViewerQueues<P> pending = new ViewerQueues<>();
    private TickScheduler.@Nullable Handle tickHandle;

    private volatile boolean hasWork;

    public PacketDispatcher(PacketSender<P> sender, TickScheduler scheduler) {
        this(sender, scheduler, Limits.DEFAULT);
    }

    public PacketDispatcher(PacketSender<P> sender, TickScheduler scheduler, Limits limits) {
        this(sender, scheduler, limits, Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "flux-packets");
            thread.setDaemon(true);
            return thread;
        }));
    }

    public PacketDispatcher(PacketSender<P> sender, TickScheduler scheduler, Limits limits, ExecutorService executor) {
        this.sender = sender;
        this.scheduler = scheduler;
        this.limits = limits;
        this.executor = executor;
    }

    public boolean hasWork() {
        return this.hasWork;
    }

    public void schedule(UUID viewer, List<P> packets) {
        if (packets.isEmpty()) return;

        List<P> copy = List.copyOf(packets);
        this.hasWork = true;

        this.executor.execute(() -> {
            this.pending.addNormal(viewer, copy, this.limits.maxBacklog());
            this.hasWork = true;
        });
    }

    public void scheduleEssential(UUID viewer, List<P> packets) {
        if (packets.isEmpty()) return;

        List<P> copy = List.copyOf(packets);
        this.hasWork = true;

        this.executor.execute(() -> {
            this.pending.addEssential(viewer, copy);
            this.hasWork = true;
        });
    }

    public void scheduleLatest(UUID viewer, Map<Integer, P> packets) {
        if (packets.isEmpty()) return;

        Map<Integer, P> copy = Map.copyOf(packets);
        this.hasWork = true;

        this.executor.execute(() -> {
            this.pending.addLatest(viewer, copy);
            this.hasWork = true;
        });
    }

    public void cancelLatest(UUID viewer, int[] keys) {
        if (keys.length == 0) return;

        this.executor.execute(() -> this.pending.cancelLatest(viewer, keys));
    }

    public synchronized void start() {
        if (this.tickHandle != null) return;

        this.tickHandle = this.scheduler.repeatEveryTick(() -> {
            if (this.hasWork) this.executor.execute(this::drain);
        });
    }

    public synchronized void stop() {
        if (this.tickHandle != null) {
            this.tickHandle.cancel();
            this.tickHandle = null;
        }

        this.executor.execute(this::flushEssentialAndClear);
        this.executor.shutdown();

        try {
            if (!this.executor.awaitTermination(3, TimeUnit.SECONDS)) {
                this.executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            this.executor.shutdownNow();
        }
    }

    private void flushEssentialAndClear() {
        this.pending.forEachEssential(this::send);
        this.pending.clear();
        this.hasWork = false;
    }

    private void drain() {
        this.pending.drain(this.limits.maxPacketsPerTick(), this::send);
        this.hasWork = !this.pending.isEmpty();
    }

    private void send(UUID viewer, List<P> batch) {
        try {
            this.sender.sendBatch(viewer, batch);
        } catch (Exception e) {
            this.sender.onFlushError(viewer, e);
        }
    }

    public record Limits(int maxPacketsPerTick, int maxBacklog) {
        public static final Limits DEFAULT = new Limits(2048, 16384);

        public Limits {
            if (maxPacketsPerTick < 1) throw new IllegalArgumentException("maxPacketsPerTick has to be positive");
            if (maxBacklog < maxPacketsPerTick) {
                throw new IllegalArgumentException("maxBacklog has to greater than or equal to maxPacketsPerTick");
            }
        }
    }
}
