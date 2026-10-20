package org.klyx.flux.paper;

import net.minecraft.network.protocol.Packet;
import org.bukkit.plugin.java.JavaPlugin;
import org.klyx.flux.FluxPlatform;
import org.klyx.flux.dispatch.PacketDispatcher;
import org.klyx.flux.dispatch.TickScheduler;
import org.slf4j.Logger;

public final class FluxPaperPlatform implements FluxPlatform {
    private final Logger logger;
    private final TickScheduler scheduler;
    private final PacketDispatcher<Packet<?>> dispatcher;

    public FluxPaperPlatform(JavaPlugin plugin) {
        this.logger = plugin.getSLF4JLogger();
        this.scheduler = new PaperTickScheduler(plugin);
        this.dispatcher = new PacketDispatcher<>(new PaperPacketSender(this.logger), this.scheduler);
    }

    @Override
    public Logger logger() {
        return this.logger;
    }

    @Override
    public TickScheduler tickScheduler() {
        return this.scheduler;
    }

    public PacketDispatcher<Packet<?>> dispatcher() {
        return this.dispatcher;
    }

    public void start() {
        this.dispatcher.start();
    }

    public void stop() {
        this.dispatcher.stop();
    }
}
