package org.klyx.flux.expr;

import java.util.List;

public sealed interface ExprNode permits ExprNode.Binary, ExprNode.Call, ExprNode.Neg, ExprNode.Not,
        ExprNode.Num, ExprNode.Ternary, ExprNode.Var
{
    enum Op {
        ADD, SUB, MUL, DIV, MOD, LT, GT, LE, GE, EQ, NE, AND, OR
    }

    record Num(double value) implements ExprNode {
    }

    record Var(String name) implements ExprNode {
    }

    record Neg(ExprNode operand) implements ExprNode {
    }

    record Not(ExprNode operand) implements ExprNode {
    }

    record Binary(Op op, ExprNode left, ExprNode right) implements ExprNode {
    }

    record Ternary(ExprNode condition, ExprNode then, ExprNode otherwise) implements ExprNode {
    }

    record Call(MathFunction function, List<ExprNode> args) implements ExprNode {
    }
}
