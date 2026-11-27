package lol.lwes.voil.script.ast;

import java.util.List;
import lol.lwes.voil.script.ScriptStatement;

public final class WhileStatement extends ScriptStatement {
    public final Expression condition;
    public final List<ScriptStatement> body;

    public WhileStatement(Expression condition, List<ScriptStatement> body) {
        this.condition = condition;
        this.body = body;
    }
}
