package org.klyx.flux.dispatch;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ViewerQueues<P> {
    private final Map<UUID, Queues<P>> pending = new HashMap<>();

    public void addNormal(UUID viewer, List<P> packets, int maxBacklog) {
        ArrayDeque<P> normal = this.pending.computeIfAbsent(viewer, _ -> new Queues<>()).normal;
        normal.addAll(packets);

        while (normal.size() > maxBacklog) normal.pollFirst();
    }

    public void addEssential(UUID viewer, List<P> packets) {
        this.pending.computeIfAbsent(viewer, _ -> new Queues<>()).essential.addAll(packets);
    }

    public boolean isEmpty() {
        return this.pending.isEmpty();
    }

    public void clear() {
        this.pending.clear();
    }

    public void forEachEssential(BatchSink<P> sink) {
        for (Map.Entry<UUID, Queues<P>> entry : this.pending.entrySet()) {
            Queues<P> queues = entry.getValue();
            if (queues.essential.isEmpty()) continue;

            sink.accept(entry.getKey(), new ArrayList<>(queues.essential));
        }
    }

    public void drain(int budget, BatchSink<P> sink) {
        for (Iterator<Map.Entry<UUID, Queues<P>>> iterator = this.pending.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<UUID, Queues<P>> entry = iterator.next();
            Queues<P> queues = entry.getValue();

            List<P> batch = takeBatch(queues, budget);

            if (queues.isEmpty()) iterator.remove();
            if (batch.isEmpty()) continue;

            sink.accept(entry.getKey(), batch);
        }
    }

    private static <P> List<P> takeBatch(Queues<P> queues, int budget) {
        List<P> batch = new ArrayList<>(Math.min(queues.essential.size() + queues.normal.size(), budget));
        while (batch.size() < budget && !queues.essential.isEmpty()) batch.add(queues.essential.pollFirst());
        while (batch.size() < budget && !queues.normal.isEmpty()) batch.add(queues.normal.pollFirst());
        return batch;
    }

    @FunctionalInterface
    public interface BatchSink<P> {
        void accept(UUID viewer, List<P> batch);
    }

    private static final class Queues<P> {
        private final ArrayDeque<P> essential = new ArrayDeque<>();
        private final ArrayDeque<P> normal = new ArrayDeque<>();

        private boolean isEmpty() {
            return this.essential.isEmpty() && this.normal.isEmpty();
        }
    }
}
