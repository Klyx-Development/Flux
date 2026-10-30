package org.klyx.flux.plugin.command;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.klyx.flux.effect.playback.Playback;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class CurrentPlaybacks {
    private final Map<String, Playback> playing = new HashMap<>();

    public void play(String particleName, Playback playback) {
        playing.put(particleName + "-" + UUID.randomUUID().toString().substring(0, 6), playback);
    }

    public void stop(String playbackId) {
        Playback previous = this.playing.get(playbackId);
        if (previous != null) previous.stop();
    }

    public int apply(CommandSourceStack source, String id, Consumer<Playback> action) {
        if (id.equals("all")) {
            playing.forEach((_, playback) -> action.accept(playback));
        }

        Playback playback = this.playing.get(id);
        if (playback == null || playback.isStopped()) {
            source.getSender().sendMessage("no effects are currently running with that id");
            return 0;
        }

        action.accept(playback);
        return Command.SINGLE_SUCCESS;
    }

    public Collection<Playback> getPlaybacks() {
        return this.playing.values();
    }

    public Collection<String> getPlaybackIds() {
        return this.playing.keySet();
    }
}
