package org.klyx.flux.blueprint;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.effect.Effect;
import org.klyx.flux.effect.ParticleLike;
import org.klyx.flux.effect.AdvancedParticle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class BlueprintRegistry {
    private static final int MAX_FILE_BYTES = 1 << 20;
    private static final String LONG_SUFFIX = ".particle.json";
    private static final String SHORT_SUFFIX = ".json";

    private final Map<String, ParticleLike> registered = new ConcurrentHashMap<>();

    private volatile Map<String, ParticleLike> loaded = Collections.emptyMap();

    public @Nullable ParticleLike get(String name) {
        ParticleLike found = this.loaded.get(name);
        return found != null ? found : this.registered.get(name);
    }

    public @Nullable Effect effect(String name) {
        return get(name) instanceof Effect effect ? effect : null;
    }

    public @Nullable AdvancedParticle system(String name) {
        return get(name) instanceof AdvancedParticle system ? system : null;
    }

    public List<String> names() {
        TreeSet<String> names = new TreeSet<>(this.loaded.keySet());
        names.addAll(this.registered.keySet());

        return List.copyOf(names);
    }

    public int size() {
        return names().size();
    }

    public void register(String name, ParticleLike particle) {
        this.registered.put(name, particle);
    }

    public Result load(Path directory) throws IOException {
        return load(List.of(directory));
    }

    public synchronized Result load(List<Path> directories) throws IOException {
        Map<String, String> sources = new TreeMap<>();
        for (Path directory : directories) {
            read(directory, sources);
        }

        Map<String, ParticleLike> previous = this.loaded;

        Map<String, ParticleLike> compiled = new LinkedHashMap<>();
        Map<String, BlueprintException> failed = new LinkedHashMap<>();
        for (String name : sources.keySet()) {
            compile(name, sources, compiled, failed, new ArrayList<>());
        }

        Map<String, ParticleLike> next = new LinkedHashMap<>(compiled);
        for (String name : failed.keySet()) {
            ParticleLike old = previous.get(name);
            if (old != null) next.put(name, old);
        }

        List<String> removed = new ArrayList<>();
        for (String name : previous.keySet()) {
            if (!sources.containsKey(name)) removed.add(name);
        }

        this.loaded = Map.copyOf(next);
        return new Result(List.copyOf(compiled.keySet()), failed, removed);
    }

    private void compile(
            String name, Map<String, String> sources, Map<String, ParticleLike> compiled,
            Map<String, BlueprintException> failed, List<String> stack
    ) {
        if (compiled.containsKey(name) || failed.containsKey(name)) return;

        stack.add(name);
        try {
            compiled.put(name, BlueprintCompiler.compileAny(sources.get(name), reference -> {
                if (stack.contains(reference)) {
                    List<String> loop = new ArrayList<>(stack.subList(stack.indexOf(reference), stack.size()));
                    loop.add(reference);
                    throw new BlueprintException(new BlueprintProblem(
                            "",
                            "effects start each other in a circle: " + String.join(" -> ", loop)
                    ));
                }

                if (!sources.containsKey(reference)) {
                    ParticleLike code = this.registered.get(reference);
                    if (code instanceof Effect effect) return effect;

                    Set<String> available = new TreeSet<>(sources.keySet());
                    available.addAll(this.registered.keySet());
                    throw new BlueprintException(new BlueprintProblem(
                            "",
                            "there's no effect named \"" + reference + "\" available"
                    ));
                }

                compile(reference, sources, compiled, failed, stack);

                ParticleLike found = compiled.get(reference);
                if (found instanceof Effect effect) return effect;

                if (found != null) {
                    throw new BlueprintException(new BlueprintProblem(
                            "",
                            "\"" + reference + "\" is a particle, only effects can be started from a cue"
                    ));
                }

                BlueprintException cause = failed.get(reference);
                if (cause == null) {
                    throw new BlueprintException(new BlueprintProblem("", "\"" + reference + "\" has its own errors"));
                }

                List<BlueprintProblem> inherited = new ArrayList<>();
                for (BlueprintProblem problem : cause.problems()) {
                    String where = problem.path().isEmpty() ? "" : problem.path() + ": ";
                    inherited.add(new BlueprintProblem("", "\"" + reference + "\" has errors: " + where + problem.message()));
                }

                throw new BlueprintException(inherited);
            }));
        } catch (BlueprintException e) {
            failed.put(name, e);
        } finally {
            stack.removeLast();
        }
    }

    private static void read(Path directory, Map<String, String> sources) throws IOException {
        if (!Files.isDirectory(directory)) return;

        List<Path> files;
        try (Stream<Path> walk = Files.walk(directory)) {
            files = walk.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(SHORT_SUFFIX))
                    .sorted()
                    .toList();
        }

        for (Path file : files) {
            String relative = directory.relativize(file).toString().replace('\\', '/');
            final var shortSubstring = relative.substring(0, relative.length() - SHORT_SUFFIX.length());
            String name = relative.endsWith(LONG_SUFFIX)
                    ? relative.substring(0, relative.length() - LONG_SUFFIX.length())
                    : shortSubstring;

            if (sources.containsKey(name)) {
                sources.put(shortSubstring, "\"another file is already called \\\"" + name + "\\\"\"");
                continue;
            }

            if (Files.size(file) > MAX_FILE_BYTES) {
                sources.put(name, "\"this file is larger than " + MAX_FILE_BYTES / 1024 + " KB\"");
                continue;
            }

            sources.put(name, Files.readString(file, StandardCharsets.UTF_8));
        }
    }

    public record Result(List<String> loaded, Map<String, BlueprintException> failed, List<String> removed) {
        public boolean ok() {
            return failed.isEmpty();
        }
    }
}
