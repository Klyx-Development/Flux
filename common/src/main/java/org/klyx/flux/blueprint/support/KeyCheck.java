package org.klyx.flux.blueprint.support;

import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;
import org.klyx.flux.blueprint.BlueprintProblem;
import org.klyx.flux.blueprint.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class KeyCheck {

    public static List<BlueprintProblem> unknownKeys(JsonObject json, String path, String... allowed) {
        Set<String> known = new TreeSet<>(Arrays.asList(allowed));
        List<BlueprintProblem> found = new ArrayList<>();

        for (String key : json.keySet()) {
            if (known.contains(key)) continue;

            String expected = "expected one of: " + String.join(", ", known);
            found.add(new BlueprintProblem(Node.childPath(path, key), "unknown key \"" + key + "\", " + expected));
        }

        return found;
    }

}
