package org.klyx.fluxtest.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.klyx.flux.effect.ParticleLike;
import org.klyx.flux.effect.AdvancedParticle;
import org.klyx.flux.effect.playback.Playback;
import org.klyx.flux.paper.FluxPaper;
import org.klyx.fluxtest.recipe.DemoEmitters;
import org.klyx.fluxtest.recipe.Recipe;

import java.util.List;
import java.util.Map;

public final class PlayCommands {
    private static final double DEFAULT_HEIGHT = 1.0;

    private final CurrentPlaybacks playbacks;
    private final Map<String, Recipe> recipes;

    public PlayCommands(CurrentPlaybacks playbacks, Map<String, Recipe> recipes) {
        this.playbacks = playbacks;
        this.recipes = recipes;
    }

    public LiteralArgumentBuilder<CommandSourceStack> play(String name, boolean follow) {
        return Commands.literal(name)
                .then(Commands.argument("name", StringArgumentType.greedyString())
                        .suggests((_, builder) -> {
                            String typed = builder.getRemainingLowerCase();
                            FluxPaper.blueprints().names().stream()
                                    .filter(candidate -> candidate.toLowerCase().startsWith(typed))
                                    .forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) return 0;

                            String wanted = StringArgumentType.getString(ctx, "name").trim();
                            ParticleLike particle = FluxPaper.blueprints().get(wanted);
                            if (particle == null) {
                                error(player, "no particle called \"" + wanted + "\"");
                                return 0;
                            }

                            this.playbacks.stop(player);
                            return start(player, wanted, particle, follow);
                        }));
    }

    private int start(Player player, String name, ParticleLike particle, boolean follow) {
        List<Player> viewers = List.of(player);
        Playback started;
        try {
            if (particle instanceof AdvancedParticle system) reportIgnored(player, system);

            started = follow ? FluxPaper.follow(particle, player, heightOffset(name), viewers)
                    : FluxPaper.play(particle, inFront(player, particle), viewers);
        } catch (IllegalArgumentException e) {
            error(player, e.getMessage());
            return 0;
        }

        this.playbacks.replace(player, started);
        return Command.SINGLE_SUCCESS;
    }

    private double heightOffset(String name) {
        Recipe recipe = name.startsWith(DemoEmitters.PREFIX)
                ? this.recipes.get(name.substring(DemoEmitters.PREFIX.length()))
                : null;

        return recipe == null ? DEFAULT_HEIGHT : recipe.heightOffset();
    }

    private static Location inFront(Player player, ParticleLike particle) {
        boolean system = particle instanceof AdvancedParticle;

        return player.getLocation()
                .add(player.getLocation().getDirection().multiply(system ? 4 : 5))
                .add(0, system ? 0.5 : 1.5, 0);
    }

    private static void reportIgnored(Player player, AdvancedParticle system) {
        if (system.ignored().isEmpty()) return;

        player.sendMessage(Component.text(
                system.ignored().size() + " thing(s) in this file aren't used:", NamedTextColor.WHITE));
        system.ignored().stream().limit(6).forEach(ignored ->
                player.sendMessage(Component.text("  " + ignored, NamedTextColor.GRAY)));
    }

    private static void error(Player player, String message) {
        player.sendMessage(Component.text(message, NamedTextColor.RED));
    }
}
