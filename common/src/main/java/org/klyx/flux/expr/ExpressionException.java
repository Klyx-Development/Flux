package org.klyx.flux.expr;

public final class ExpressionException extends Exception {
    private final int position;

    public ExpressionException(String message, int position) {
        super(message + " (at character " + (position + 1) + ")");
        this.position = position;
    }

    private ExpressionException(String message, int position, boolean exact) {
        super(message);
        this.position = position;
    }

    public static ExpressionException exact(String message, int position) {
        return new ExpressionException(message, position, true);
    }

    public int position() {
        return this.position;
    }
}
