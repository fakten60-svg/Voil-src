package lol.lwes.voil.script.ast;

public final class AssignExpression extends BindingExpression {
    public AssignExpression(String name, Expression value) {
        super(name, value);
    }
}
