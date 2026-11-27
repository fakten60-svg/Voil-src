package lol.lwes.voil.script.ast;

public abstract class BindingExpression extends Expression {
    public final String name;
    public final Expression value;

    protected BindingExpression(String name, Expression value) {
        this.name = name;
        this.value = value;
    }
}
