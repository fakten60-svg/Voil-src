package lol.lwes.voil.script;

import lol.lwes.voil.script.ast.Expression;

public class ScriptVarStmt extends ScriptStatement {
    public final String name;
    public final Expression value;

    public ScriptVarStmt(String name, Expression value) {
        this.name = name;
        this.value = value;
    }
}
