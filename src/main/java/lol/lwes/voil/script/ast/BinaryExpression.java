package lol.lwes.voil.script.ast;

public final class BinaryExpression extends Expression {
    public final Expression left;
    public final String operator;
    public final Expression right;

    public BinaryExpression(Expression left, String operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
}
