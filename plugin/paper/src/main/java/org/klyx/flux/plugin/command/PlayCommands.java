package org.klyx.flux.plugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.klyx.flux.effect.ParticleLike;
import org.klyx.flux.effect.AdvancedParticle;
import org.klyx.flux.effect.playback.Playback;
import org.klyx.flux.paper.FluxPaper;
import org.klyx.flux.plugin.recipe.DemoEmitters;
import org.klyx.flux.plugin.recipe.Recipe;

import java.util.Collection;
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
                            return execute(player, player.getLocation(), wanted, follow);
                        }));
    }

    private int execute(Player player, Location targetLocation, String particleName, boolean follow) {
        ParticleLike particle = FluxPaper.blueprints().get(particleName);
        if (particle == null) {
            error(player, "no particle called \"" + particleName + "\"");
            return 0;
        }

        return start(player, targetLocation, particleName, particle, follow);
    }

    private int start(Player player, Location targetLocation, String name, ParticleLike particle, boolean follow) {
        var viewers = Bukkit.getOnlinePlayers();
        Playback started;
        try {
            if (particle instanceof AdvancedParticle system) reportIgnored(player, system);

            started = follow ? FluxPaper.follow(particle, player, heightOffset(name), viewers)
                    : FluxPaper.play(particle, targetLocation, viewers);
        } catch (IllegalArgumentException e) {
            error(player, e.getMessage());
            return 0;
        }

        this.playbacks.play(name, started);
        return Command.SINGLE_SUCCESS;
    }

    private double heightOffset(String name) {
        Recipe recipe = name.startsWith(DemoEmitters.PREFIX)
                ? this.recipes.get(name.substring(DemoEmitters.PREFIX.length()))
                : null;

        return recipe == null ? DEFAULT_HEIGHT : recipe.heightOffset();
    }


    private static void reportIgnored(Player player, AdvancedParticle system) {
        if (system.ignored().isEmpty()) return;

        player.sendMessage(Component.text(
                system.ignored().size() + " thing(s) in this file aren't used:", NamedTextColor.WHITE));
        system.ignored().stream().limit(6).forEach(ignored ->
                player.sendMessage(Component.text("  " + ignored, NamedTextColor.GRAY)));
    }

    private static void error(CommandSender sender, String message) {
        sender.sendMessage(Component.text(message, NamedTextColor.RED));
    }
}
