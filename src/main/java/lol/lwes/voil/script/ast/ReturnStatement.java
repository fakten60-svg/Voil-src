package lol.lwes.voil.script.ast;

import lol.lwes.voil.script.ScriptStatement;

public final class ReturnStatement extends ScriptStatement {
    public final Expression value;

    public ReturnStatement(Expression value) {
        this.value = value;
    }
}
