package org.klyx.flux.expr;

import org.klyx.flux.expr.parse.Parser;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public final class Expression {
    private final String source;
    private final ExprNode root;
    private final Set<String> variables;
    private final boolean constant;

    private Expression(String source, ExprNode root) {
        this.source = source;
        this.root = root;

        TreeSet<String> found = new TreeSet<>();
        this.constant = scan(root, found);
        this.variables = Collections.unmodifiableSet(found);
    }

    public static Expression parse(String source) throws ExpressionException {
        return new Expression(source, Parser.parse(source));
    }

    public static Expression constant(double value) {
        return new Expression(Double.toString(value), new ExprNode.Num(value));
    }

    public Expression map(double add, double multiply) {
        ExprNode shifted = new ExprNode.Binary(ExprNode.Op.ADD, this.root, new ExprNode.Num(add));
        return new Expression("(" + this.source + " + " + add + ") * " + multiply,
                new ExprNode.Binary(ExprNode.Op.MUL, shifted, new ExprNode.Num(multiply)));
    }

    public double eval(Variables variables) {
        return eval(this.root, variables);
    }

    public double evalConstant() {
        if (!this.constant) throw new IllegalStateException("\"" + this.source + "\" isn't constant");
        return eval(Variables.NONE);
    }

    public boolean isConstant() {
        return this.constant;
    }

    public Set<String> variables() {
        return this.variables;
    }

    public String source() {
        return this.source;
    }

    @Override
    public String toString() {
        return this.source;
    }

    private static boolean scan(ExprNode node, Set<String> found) {
        return switch (node) {
            case ExprNode.Num _ -> true;
            case ExprNode.Var variable -> {
                found.add(variable.name());
                yield false;
            }
            case ExprNode.Neg negation -> scan(negation.operand(), found);
            case ExprNode.Not not -> scan(not.operand(), found);
            case ExprNode.Binary binary -> scan(binary.left(), found) & scan(binary.right(), found);
            case ExprNode.Ternary ternary -> scan(ternary.condition(), found)
                    & scan(ternary.then(), found)
                    & scan(ternary.otherwise(), found);
            case ExprNode.Call call -> {
                boolean all = call.function() != MathFunction.RANDOM;
                for (ExprNode argument : call.args()) {
                    all &= scan(argument, found);
                }

                yield all;
            }
        };
    }

    private static double eval(ExprNode node, Variables variables) {
        return switch (node) {
            case ExprNode.Num number -> number.value();
            case ExprNode.Var variable -> variables.get(variable.name());
            case ExprNode.Neg negation -> -eval(negation.operand(), variables);
            case ExprNode.Not not -> eval(not.operand(), variables) == 0 ? 1 : 0;
            case ExprNode.Ternary ternary -> eval(ternary.condition(), variables) != 0
                    ? eval(ternary.then(), variables)
                    : eval(ternary.otherwise(), variables);
            case ExprNode.Binary binary -> binary(binary, variables);
            case ExprNode.Call call -> call(call, variables);
        };
    }

    private static double binary(ExprNode.Binary binary, Variables variables) {
        if (binary.op() == ExprNode.Op.AND) {
            return eval(binary.left(), variables) != 0 && eval(binary.right(), variables) != 0 ? 1 : 0;
        }

        if (binary.op() == ExprNode.Op.OR) {
            return eval(binary.left(), variables) != 0 || eval(binary.right(), variables) != 0 ? 1 : 0;
        }

        double left = eval(binary.left(), variables);
        double right = eval(binary.right(), variables);

        return switch (binary.op()) {
            case ADD -> left + right;
            case SUB -> left - right;
            case MUL -> left * right;
            case DIV -> right == 0 ? 0 : left / right;
            case MOD -> right == 0 ? 0 : left % right;
            case LT -> left < right ? 1 : 0;
            case GT -> left > right ? 1 : 0;
            case LE -> left <= right ? 1 : 0;
            case GE -> left >= right ? 1 : 0;
            case EQ -> left == right ? 1 : 0;
            case NE -> left != right ? 1 : 0;
            case AND, OR -> throw new AssertionError("handled above");
        };
    }

    private static double call(ExprNode.Call call, Variables variables) {
        double[] arguments = new double[call.args().size()];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = eval(call.args().get(i), variables);
        }

        return call.function().apply(arguments);
    }
}
