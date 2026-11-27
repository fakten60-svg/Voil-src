package lol.lwes.voil.script.ast;

public final class UnaryExpression extends Expression {
    public final String operator;
    public final Expression operand;

    public UnaryExpression(String operator, Expression operand) {
        this.operator = operator;
        this.operand = operand;
    }
}
