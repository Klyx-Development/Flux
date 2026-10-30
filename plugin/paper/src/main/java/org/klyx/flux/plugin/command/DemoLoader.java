package org.klyx.flux.plugin.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.klyx.flux.blueprint.BlueprintException;
import org.klyx.flux.blueprint.BlueprintProblem;
import org.klyx.flux.blueprint.BlueprintRegistry;
import org.klyx.flux.paper.FluxPaper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class DemoLoader {
    private final List<Path> directories;

    public DemoLoader(List<Path> directories) {
        this.directories = List.copyOf(directories);
    }

    public void reload(CommandSender sender) {
        BlueprintRegistry.Result result;
        try {
            result = FluxPaper.blueprints().load(this.directories);
        } catch (IOException e) {
            sender.sendMessage(Component.text("Couldn't read the example folders: " + e.getMessage(), NamedTextColor.RED));
            return;
        }

        sender.sendMessage(Component.text("loaded " + result.loaded().size() + " file(s)"
                + (result.removed().isEmpty() ? "" : ", removed " + result.removed().size())
                + (result.failed().isEmpty() ? "." : ", " + result.failed().size() + " with errors:"),
                result.ok() ? NamedTextColor.GREEN : NamedTextColor.YELLOW));
        reportFailures(sender, result.failed());
    }

    private static void reportFailures(CommandSender sender, Map<String, BlueprintException> failed) {
        failed.forEach((name, error) -> {
            sender.sendMessage(Component.text(name, NamedTextColor.RED));
            for (BlueprintProblem problem : error.problems()) {
                sender.sendMessage(Component.text("  " + problem, NamedTextColor.GRAY));
            }
        });
    }
}
