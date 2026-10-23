package org.klyx.flux.paper;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;
import org.klyx.exo.Exo;
import org.klyx.exo.paper.ExoPaper;
import org.klyx.flux.Flux;
import org.klyx.flux.blueprint.BlueprintRegistry;
import org.klyx.flux.effect.Effect;
import org.klyx.flux.effect.ParticleLike;
import org.klyx.flux.effect.AdvancedParticle;
import org.klyx.flux.effect.playback.Playback;
import org.klyx.flux.paper.display.DisplayParticles;

import java.util.Collection;

public final class FluxPaper {
    private static final BlueprintRegistry BLUEPRINTS = new BlueprintRegistry();

    private static @Nullable FluxPaperPlatform platform;
    private static @Nullable ParticleRenderer renderer;
    private static @Nullable PaperEffects effects;
    private static @Nullable DisplayParticles displayParticles;
    private static @Nullable EntityFollowers followers;
    private static boolean ownsExo;

    public static synchronized void init(JavaPlugin plugin) {
        if (platform != null) throw new IllegalStateException("Flux already initialised");

        // if you're using Exo, please register your own exo in your plugin before calling FluxPaper#init!
        ownsExo = !exoIsRunning();
        if (ownsExo) ExoPaper.init(plugin);

        FluxPaperPlatform created = new FluxPaperPlatform(plugin);
        Flux.init(created);
        created.start();

        platform = created;
        renderer = new ParticleRenderer(created);
        followers = new EntityFollowers(plugin);
        effects = new PaperEffects(plugin, renderer, followers);
        displayParticles = new DisplayParticles(plugin, followers, created.dispatcher());
    }

    public static synchronized void destroy() {
        FluxPaperPlatform current = platform;
        if (current == null) throw new IllegalStateException("Flux not initialised");

        // close vanilla particles first, removing display entities needs the dispatcher which stop() flushes before closing
        if (displayParticles != null) displayParticles.close();
        if (effects != null) effects.shutdown();
        if (followers != null) followers.close();
        current.stop();

        if (ownsExo) {
            ExoPaper.destroy();
            ownsExo = false;
        }

        Flux.destroy();
        platform = null;
        renderer = null;
        effects = null;
        displayParticles = null;
        followers = null;
    }

    public static ParticleRenderer renderer() {
        ParticleRenderer current = renderer;
        if (current == null) throw new IllegalStateException("Flux not initialised");

        return current;
    }

    public static BlueprintRegistry blueprints() {
        return BLUEPRINTS;
    }

    public static Playback play(ParticleLike particle, Location location, Collection<? extends Player> viewers) {
        return switch (particle) {
            case Effect effect -> effects().play(effect, location, viewers);
            case AdvancedParticle system -> displayParticles().play(system.emitter(), system.particle(), location, viewers);
        };
    }

    public static Playback follow(
            ParticleLike particle, Entity entity, double heightOffset, Collection<? extends Player> viewers
    ) {
        return switch (particle) {
            case Effect effect -> effects().follow(effect, entity, heightOffset, viewers);
            case AdvancedParticle system ->
                    displayParticles().follow(system.emitter(), system.particle(), entity, heightOffset, viewers);
        };
    }

    public static DisplayParticles displayParticles() {
        DisplayParticles current = displayParticles;
        if (current == null) throw new IllegalStateException("Flux not initialised");

        return current;
    }

    public static PaperEffects effects() {
        PaperEffects current = effects;
        if (current == null) throw new IllegalStateException("Flux not initialised");

        return current;
    }

    private static boolean exoIsRunning() {
        try {
            Exo.platform();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }
}
