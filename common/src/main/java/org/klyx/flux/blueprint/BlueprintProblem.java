package org.klyx.flux.blueprint;

public record BlueprintProblem(String path, String message) {
    @Override
    public String toString() {
        return path.isEmpty() ? message : path + ": " + message;
    }
}
