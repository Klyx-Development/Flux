package org.klyx.flux.expr.parse;

import org.jspecify.annotations.Nullable;
import org.klyx.flux.expr.ExprNode;
import org.klyx.flux.expr.Expression;
import org.klyx.flux.expr.ExpressionException;
import org.klyx.flux.expr.MathFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Parser {
    private static final Map<String, String> PREFIXES = Map.of(
            "q", "query", "query", "query",
            "v", "variable", "variable", "variable",
            "t", "temp", "temp", "temp",
            "c", "context", "context", "context");

    private final Cursor cursor;

    private Parser(String source) {
        this.cursor = new Cursor(source);
    }

    public static ExprNode parse(String source) throws ExpressionException {
        Parser parser = new Parser(source);
        parser.cursor.skipSpace();
        if (parser.cursor.atEnd()) throw new ExpressionException("Expression is empty", 0);

        ExprNode node = parser.ternary();
        parser.cursor.skipSpace();
        if (!parser.cursor.atEnd()) {
            throw new ExpressionException("Unexpected \"" + parser.cursor.current() + "\"", parser.cursor.position());
        }

        return node;
    }

    private ExprNode ternary() throws ExpressionException {
        ExprNode condition = binary(0);
        this.cursor.skipSpace();
        if (!this.cursor.eat('?')) return condition;

        ExprNode then = ternary();
        this.cursor.skipSpace();
        if (!this.cursor.eat(':')) {
            throw new ExpressionException("Expected \":\" to finish the \"?\" condition", this.cursor.position());
        }

        return new ExprNode.Ternary(condition, then, ternary());
    }

    private ExprNode binary(int level) throws ExpressionException {
        if (level == Operators.BINARY_LEVELS.size()) return unary();

        ExprNode left = binary(level + 1);
        while (true) {
            Operators.Symbol symbol = eatSymbol(Operators.BINARY_LEVELS.get(level));
            if (symbol == null) return left;

            left = new ExprNode.Binary(symbol.op(), left, binary(level + 1));
        }
    }

    private Operators.@Nullable Symbol eatSymbol(List<Operators.Symbol> symbols) {
        for (Operators.Symbol symbol : symbols) {
            if (this.cursor.eatOperator(symbol.text())) return symbol;
        }

        return null;
    }

    private ExprNode unary() throws ExpressionException {
        this.cursor.skipSpace();
        if (this.cursor.eat('-')) return new ExprNode.Neg(unary());

        if (this.cursor.current() == '!' && this.cursor.peekAt(1) != '=') {
            this.cursor.advance();
            return new ExprNode.Not(unary());
        }

        return primary();
    }

    private ExprNode primary() throws ExpressionException {
        this.cursor.skipSpace();
        if (this.cursor.atEnd()) throw new ExpressionException("Expression ends too soon", this.cursor.position());

        char character = this.cursor.current();
        if (character == '(') return parenthesized();
        if (Character.isDigit(character) || character == '.') return number();
        if (Character.isLetter(character) || character == '_') return name();

        throw new ExpressionException("Unexpected \"" + character + "\"", this.cursor.position());
    }

    private ExprNode parenthesized() throws ExpressionException {
        this.cursor.advance();
        ExprNode inner = ternary();
        this.cursor.skipSpace();
        if (!this.cursor.eat(')')) throw new ExpressionException("Missing \")\"", this.cursor.position());

        return inner;
    }

    private ExprNode number() throws ExpressionException {
        int start = this.cursor.position();
        String text = this.cursor.takeWhile(character -> Character.isDigit(character) || character == '.');

        if (this.cursor.current() == 'f' || this.cursor.current() == 'F') this.cursor.advance();

        try {
            return new ExprNode.Num(Double.parseDouble(text));
        } catch (NumberFormatException e) {
            throw new ExpressionException("\"" + text + "\" isn't a number", start);
        }
    }

    private ExprNode name() throws ExpressionException {
        int start = this.cursor.position();
        String word = this.cursor.takeWhile(Parser::isNameCharacter).toLowerCase(Locale.ROOT);

        if (word.equals("true")) return new ExprNode.Num(1);
        if (word.equals("false")) return new ExprNode.Num(0);

        int dot = word.indexOf('.');
        if (dot < 0) {
            throw new ExpressionException("Unknown name \"" + word + "\", try using query.time or variable.size instead", start);
        }

        String prefix = word.substring(0, dot);
        String rest = word.substring(dot + 1);
        if (rest.isEmpty() || rest.endsWith(".")) throw new ExpressionException("\"" + word + "\" is missing a name", start);

        if (prefix.equals("math")) {
            if (rest.equals("pi")) return new ExprNode.Num(Math.PI);
            return call(rest, start);
        }

        String canonical = PREFIXES.get(prefix);
        if (canonical == null) {
            throw new ExpressionException("Unknown prefix \"" + prefix + "\", use query, variable, temp, context or math", start);
        }

        return new ExprNode.Var(canonical + "." + rest);
    }

    private ExprNode call(String functionName, int start) throws ExpressionException {
        MathFunction function = MathFunction.byName(functionName);
        if (function == null) {
            throw new ExpressionException("Unknown function math." + functionName + ", use one of: " + MathFunction.names(), start);
        }

        this.cursor.skipSpace();
        if (!this.cursor.eat('(')) {
            throw new ExpressionException("math." + functionName + " needs arguments in parentheses", this.cursor.position());
        }

        List<ExprNode> args = arguments(functionName);
        if (args.size() != function.arity()) {
            throw new ExpressionException("math." + functionName + " takes " + function.arity() + " argument(s) but got " + args.size(), start);
        }

        return new ExprNode.Call(function, args);
    }

    private List<ExprNode> arguments(String functionName) throws ExpressionException {
        List<ExprNode> args = new ArrayList<>();
        this.cursor.skipSpace();
        if (this.cursor.eat(')')) return args;

        do {
            args.add(ternary());
            this.cursor.skipSpace();
        } while (this.cursor.eat(','));

        if (!this.cursor.eat(')')) {
            throw new ExpressionException("Missing \")\" after the arguments of math." + functionName, this.cursor.position());
        }

        return args;
    }

    private static boolean isNameCharacter(char character) {
        return Character.isLetterOrDigit(character) || character == '_' || character == '.';
    }
}
