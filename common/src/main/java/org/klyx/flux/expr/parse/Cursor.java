package org.klyx.flux.expr.parse;

public final class Cursor {
    private final String source;
    private int position;

    public Cursor(String source) {
        this.source = source;
    }

    public int position() {
        return this.position;
    }

    public boolean atEnd() {
        return this.position >= this.source.length();
    }

    public char current() {
        return peekAt(0);
    }

    public char peekAt(int offset) {
        return this.position + offset < this.source.length() ? this.source.charAt(this.position + offset) : '\0';
    }

    public void advance() {
        this.position++;
    }

    public void skipSpace() {
        while (!atEnd() && Character.isWhitespace(this.source.charAt(this.position))) this.position++;
    }

    public boolean eat(char expected) {
        if (current() != expected) return false;

        this.position++;
        return true;
    }

    public boolean eatOperator(String operator) {
        skipSpace();
        if (!this.source.startsWith(operator, this.position)) return false;

        if ((operator.equals("<") || operator.equals(">")) && peekAt(operator.length()) == '=') return false;

        this.position += operator.length();
        return true;
    }

    public String takeWhile(CharPredicate predicate) {
        int start = this.position;
        while (!atEnd() && predicate.test(this.source.charAt(this.position))) this.position++;

        return this.source.substring(start, this.position);
    }

    @FunctionalInterface
    public interface CharPredicate {
        boolean test(char character);
    }
}
