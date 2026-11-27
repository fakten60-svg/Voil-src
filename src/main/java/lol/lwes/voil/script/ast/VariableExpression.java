package lol.lwes.voil.script.ast;

public final class VariableExpression extends Expression {
    public final String name;

    public VariableExpression(String name) {
        this.name = name;
    }
}
