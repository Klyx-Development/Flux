package org.klyx.fluxtest.command;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.klyx.flux.effect.playback.Playback;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public final class CurrentPlaybacks {
    private final Map<Player, Playback> current = new WeakHashMap<>();

    public void replace(Player player, Playback started) {
        this.current.put(player, started);
    }

    public void stop(Player player) {
        Playback previous = this.current.get(player);
        if (previous != null) previous.stop();
    }

    public int apply(CommandSourceStack source, Consumer<Playback> action) {
        if (!(source.getSender() instanceof Player player)) return 0;

        Playback playback = this.current.get(player);
        if (playback == null || playback.isStopped()) {
            player.sendMessage("no effects are currently running for you");
            return 0;
        }

        action.accept(playback);
        return Command.SINGLE_SUCCESS;
    }
}
