package org.klyx.flux.blueprint.reader;

import org.klyx.flux.blueprint.BlueprintProblem;
import org.klyx.flux.blueprint.EffectResolver;

import java.util.List;

public record CompileContext(List<BlueprintProblem> problems, EffectResolver resolver) {
}
