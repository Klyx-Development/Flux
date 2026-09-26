package org.klyx.flux.expr.parse;

import org.klyx.flux.expr.ExprNode;

import java.util.List;

public final class Operators {
    public static final List<List<Symbol>> BINARY_LEVELS = List.of(
            List.of(new Symbol("||", ExprNode.Op.OR)),
            List.of(new Symbol("&&", ExprNode.Op.AND)),
            List.of(new Symbol("==", ExprNode.Op.EQ), new Symbol("!=", ExprNode.Op.NE)),
            List.of(new Symbol("<=", ExprNode.Op.LE), new Symbol(">=", ExprNode.Op.GE),
                    new Symbol("<", ExprNode.Op.LT), new Symbol(">", ExprNode.Op.GT)),
            List.of(new Symbol("+", ExprNode.Op.ADD), new Symbol("-", ExprNode.Op.SUB)),
            List.of(new Symbol("*", ExprNode.Op.MUL), new Symbol("/", ExprNode.Op.DIV), new Symbol("%", ExprNode.Op.MOD)));

    public record Symbol(String text, ExprNode.Op op) { }
}
