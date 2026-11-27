package lol.lwes.voil.script;

import lol.lwes.voil.script.ast.Expression;

public class ScriptExprStmt extends ScriptStatement {
    public final Expression expression;

    public ScriptExprStmt(Expression expression) {
        this.expression = expression;
    }
}
