package org.klyx.flux.blueprint;

import java.util.List;
import java.util.stream.Collectors;

public final class BlueprintException extends Exception {
    private final List<BlueprintProblem> problems;

    public BlueprintException(List<BlueprintProblem> problems) {
        super(problems.size() + (problems.size() == 1 ? " problem" : " problems") + ":\n"
                + problems.stream().map(problem -> " " + problem).collect(Collectors.joining("\n")));

        this.problems = List.copyOf(problems);
    }

    public BlueprintException(BlueprintProblem problem) {
        this(List.of(problem));
    }

    public List<BlueprintProblem> problems() {
        return this.problems;
    }
}
