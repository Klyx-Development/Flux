package org.klyx.flux.dispatch;

public interface TickScheduler {
    interface Handle {
        void cancel();
    }

    Handle repeatEveryTick(Runnable task);
}
