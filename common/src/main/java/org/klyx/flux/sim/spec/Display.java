package org.klyx.flux.sim.spec;

import org.jspecify.annotations.Nullable;

import java.util.regex.Pattern;

public record Display(DisplayKind kind, String item, @Nullable String model, boolean tint) {
    private static final Pattern ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");

    public Display {
        if (!ID.matcher(item).matches()) {
            throw new IllegalArgumentException("item has to have an id in the format of namespace:name e.g. minecraft:egg: \"" + item + "\""
            );
        }

        if (model != null && !ID.matcher(model).matches()) {
            throw new IllegalArgumentException(
                    "model has to have an id in the format of namespace:name e.g. minecraft:egg, or null for none: \"" + model + "\""
            );
        }
    }
}
