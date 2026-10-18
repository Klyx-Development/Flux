package org.klyx.flux.importer.snowstorm.reading;

import org.klyx.flux.blueprint.Dynamic;
import org.klyx.flux.blueprint.Scope;

import java.util.Set;

public final class Scopes {

    public static Scope allowing(Set<String> curveNames, Set<String> custom) {
        return variable -> {
            if (curveNames.contains(variable) || custom.contains(variable)) return null;

            String problem = Dynamic.EMITTER.problem(variable);
            if (problem == null) return null;

            return custom.isEmpty()
                    ? problem + ". to use your own variable, set it in emitter_initialization, like \"variable.size = 0.1;\""
                    : problem + ". set it in emitter_initialization, available: " + String.join(", ", custom);
        };
    }
}
