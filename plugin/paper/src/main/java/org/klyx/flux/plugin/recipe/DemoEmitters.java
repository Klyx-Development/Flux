package org.klyx.flux.plugin.recipe;

import org.klyx.flux.blueprint.BlueprintRegistry;
import org.klyx.flux.plugin.recipe.impl.BobbersRecipe;
import org.klyx.flux.plugin.recipe.impl.BurstRecipe;
import org.klyx.flux.plugin.recipe.impl.CrownRecipe;
import org.klyx.flux.plugin.recipe.impl.StreamersRecipe;

import java.util.Map;

public final class DemoEmitters {
    public static final String PREFIX = "recipe/";

    public static void register(BlueprintRegistry registry) {
        all().forEach((name, recipe) -> registry.register(PREFIX + name, recipe.system()));
    }

    public static Map<String, Recipe> all() {
        return Map.ofEntries(
                Map.entry("burst", BurstRecipe.create()),
                Map.entry("bobbers", BobbersRecipe.create()),
                Map.entry("streamers", StreamersRecipe.create()),
                Map.entry("crown", CrownRecipe.create()));
    }
}
