package org.klyx.flux.blueprint;

import org.klyx.flux.effect.Effect;

@FunctionalInterface
public interface EffectResolver {
    EffectResolver NONE = name -> {
        throw new BlueprintException(new BlueprintProblem("", "there is no effect named \"" + name + "\""));
    };

    Effect resolve(String name) throws BlueprintException;
}
