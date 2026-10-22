package org.klyx.fluxtest.recipe;

import org.klyx.flux.blueprint.BlueprintRegistry;
import org.klyx.fluxtest.recipe.impl.BobbersRecipe;
import org.klyx.fluxtest.recipe.impl.BurstRecipe;
import org.klyx.fluxtest.recipe.impl.CrownRecipe;
import org.klyx.fluxtest.recipe.impl.StreamersRecipe;

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
