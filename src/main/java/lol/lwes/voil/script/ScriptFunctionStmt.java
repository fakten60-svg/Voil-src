package lol.lwes.voil.script;

import java.util.List;

public class ScriptFunctionStmt extends ScriptStatement {
    public final String name;
    public final List<String> params;
    public final List<ScriptStatement> body;

    public ScriptFunctionStmt(String name, List<String> params, List<ScriptStatement> body) {
        this.name = name;
        this.params = params;
        this.body = body;
    }
}
