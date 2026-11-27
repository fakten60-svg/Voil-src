package lol.lwes.voil.script.ast;

import java.util.List;
import lol.lwes.voil.script.ScriptStatement;

public final class IfStatement extends ScriptStatement {
    public final Expression condition;
    public final List<ScriptStatement> thenBranch;
    public final List<ScriptStatement> elseBranch;

    public IfStatement(Expression condition, List<ScriptStatement> thenBranch, List<ScriptStatement> elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public boolean hasElse() {
        return this.elseBranch != null;
    }
}
