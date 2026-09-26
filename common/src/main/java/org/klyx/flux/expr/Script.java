package org.klyx.flux.expr;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public final class Script {
    public record Assignment(String name, Expression value) { }

    public static final Script EMPTY = new Script(List.of());

    private static final Pattern NAME = Pattern.compile("[a-z_][a-z0-9_]*");

    private final List<Assignment> assignments;

    private Script(List<Assignment> assignments) {
        this.assignments = List.copyOf(assignments);
    }

    public static Script parse(String source) throws ExpressionException {
        List<Assignment> assignments = new ArrayList<>();
        int start = 0;
        while (true) {
            int end = source.indexOf(';', start);
            if (end < 0) end = source.length();

            String raw = source.substring(start, end);
            String statement = raw.strip();
            if (!statement.isEmpty()) {
                int leading = raw.length() - raw.stripLeading().length();
                assignments.add(statement(statement, start + leading));
            }

            if (end == source.length()) break;
            start = end + 1;
        }

        return assignments.isEmpty() ? EMPTY : new Script(assignments);
    }

    public List<Assignment> assignments() {
        return this.assignments;
    }

    public boolean isEmpty() {
        return this.assignments.isEmpty();
    }

    public Set<String> assignedNames() {
        Set<String> names = new TreeSet<>();
        for (Assignment assignment : this.assignments) {
            names.add(assignment.name());
        }
        return names;
    }

    private static Assignment statement(String statement, int offset) throws ExpressionException {
        int equals = assignmentIndex(statement);
        if (equals < 0) {
            throw ExpressionException.exact(
                    "expected an assignment like variable.size = 0.1, but found \"" + statement + "\"" + at(offset), offset
            );
        }

        String target = statement.substring(0, equals).strip().toLowerCase(Locale.ROOT);
        if (target.isEmpty()) {
            throw ExpressionException.exact("Missing the variable name before \"=\"" + at(offset), offset);
        }
        int dot = target.indexOf('.');
        String prefix = dot < 0 ? "" : target.substring(0, dot);
        String canonical = switch (prefix) {
            case "variable", "v" -> "variable";
            case "temp", "t" -> "temp";
            default -> null;
        };
        if (canonical == null) {
            throw ExpressionException.exact(
                    "\"" + target + "\" isn't assigned to variable.* or temp.*" + at(offset), offset
            );
        }
        String local = target.substring(dot + 1);
        if (!NAME.matcher(local).matches()) {
            throw ExpressionException.exact("\"" + local + "\" isn't a valid variable name" + at(offset), offset);
        }

        int valueStart = offset + equals + 1;
        try {
            return new Assignment(canonical + "." + local, Expression.parse(statement.substring(equals + 1)));
        } catch (ExpressionException e) {
            String inner = e.getMessage().replaceFirst(" \\(at character \\d+\\)$", "");
            int position = valueStart + e.position();
            throw ExpressionException.exact("In \"" + statement + "\": " + inner + at(position), position);
        }
    }

    private static int assignmentIndex(String statement) {
        for (int i = 0; i < statement.length(); i++) {
            if (statement.charAt(i) != '=') continue;
            if (i + 1 < statement.length() && statement.charAt(i + 1) == '=') {
                i++;
                continue;
            }

            char before = i > 0 ? statement.charAt(i - 1) : 0;
            if (before == '!' || before == '<' || before == '>' || before == '=') continue;
            return i;
        }
        return -1;
    }

    private static String at(int offset) {
        return " (at character " + (offset + 1) + ")";
    }
}
