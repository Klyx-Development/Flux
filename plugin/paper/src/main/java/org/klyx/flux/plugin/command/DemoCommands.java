package org.klyx.flux.plugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.klyx.flux.effect.playback.Playback;
import org.klyx.flux.paper.FluxPaper;
import org.klyx.flux.plugin.recipe.Recipe;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class DemoCommands {
    private final CurrentPlaybacks playbacks;
    private final PlayCommands play;
    private final DemoLoader loader;

    public DemoCommands(JavaPlugin plugin, DemoLoader loader, Map<String, Recipe> recipes) {
        this.playbacks = new CurrentPlaybacks();
        this.play = new PlayCommands(this.playbacks, recipes);
        this.loader = loader;
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("flux")
                .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("flux.admin"))
                .then(this.play.play("play", false))
                .then(this.play.play("follow", true))
                .then(control("pause", Playback::pause))
                .then(control("resume", Playback::resume))
                .then(control("stop", Playback::stop))
                .then(list())
                .then(reload())
                .build();
    }

    private LiteralArgumentBuilder<CommandSourceStack> control(String name, Consumer<Playback> action) {
        return Commands.literal(name)
                .then(Commands.argument("particleId", StringArgumentType.greedyString())
                        .suggests((_, builder) -> {
                            String typed = builder.getRemainingLowerCase();
                            Collection<String> playbacks = this.playbacks.getPlaybackIds();
                            playbacks.add("all");

                            playbacks.stream()
                                    .filter(candidate -> candidate.toLowerCase().startsWith(typed.toLowerCase()))
                                    .forEach(builder::suggest);

                            return builder.buildFuture();
                        })
                        .executes(ctx ->
                                playbacks.apply(ctx.getSource(), StringArgumentType.getString(ctx, "particleId"), action)
                        )
                );
    }

    private LiteralArgumentBuilder<CommandSourceStack> list() {
        return Commands.literal("list").executes(ctx -> {
            CommandSender sender = ctx.getSource().getSender();

            List<String> names = FluxPaper.blueprints().names();
            sender.sendMessage(Component.text(
                    names.isEmpty() ? "nothing loaded, maybe you should make some particles :)" : "Available: " + String.join(", ", names)));
            return Command.SINGLE_SUCCESS;
        });
    }

    private LiteralArgumentBuilder<CommandSourceStack> reload() {
        return Commands.literal("reload").executes(ctx -> {
            this.loader.reload(ctx.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        });
    }
}
