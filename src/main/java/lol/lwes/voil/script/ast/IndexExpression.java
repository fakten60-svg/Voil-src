package lol.lwes.voil.script.ast;

public final class IndexExpression extends Expression {
    public final Expression target;
    public final Expression index;

    public IndexExpression(Expression target, Expression index) {
        this.target = target;
        this.index = index;
    }
}
