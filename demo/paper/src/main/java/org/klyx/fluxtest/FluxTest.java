package org.klyx.fluxtest;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import org.klyx.flux.paper.FluxPaper;
import org.klyx.fluxtest.command.DemoCommands;
import org.klyx.fluxtest.command.DemoLoader;
import org.klyx.fluxtest.recipe.DemoEmitters;

import java.nio.file.Path;
import java.util.List;

public final class FluxTest extends JavaPlugin {
    private static final List<String> BUNDLED_EFFECTS = List.of("burst", "flame_ring", "halo", "pulse_wave", "ripple");
    private static final List<String> BUNDLED_EMITTERS = List.of("fountain", "sparks");
    private static final List<String> BUNDLED_SNOWSTORM = List.of("amethyst_burst", "bouncy_balls", "bubbles", "comet", "fire_ring", "fireworks", "rainbow");

    @Override
    public void onEnable() {
        FluxPaper.init(this);

        Path effects = getDataPath().resolve("effects");
        Path snowstorm = getDataPath().resolve("snowstorm");
        Path emitters = getDataPath().resolve("emitters");
        new ExampleFiles(this, "effects examples", "effects", ".json", BUNDLED_EFFECTS, effects).copyMissing();
        new ExampleFiles(this, "snowstorm examples", "snowstorm", ".particle.json", BUNDLED_SNOWSTORM, snowstorm)
                .copyMissing();
        new ExampleFiles(this, "emitter examples", "emitters", ".json", BUNDLED_EMITTERS, emitters).copyMissing();

        DemoEmitters.register(FluxPaper.blueprints());

        DemoLoader loader = new DemoLoader(List.of(effects, snowstorm, emitters));
        loader.reload(getServer().getConsoleSender());

        DemoCommands commands = new DemoCommands(loader, DemoEmitters.all());
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(commands.build()));
    }

    @Override
    public void onDisable() {
        FluxPaper.destroy();
    }
}
