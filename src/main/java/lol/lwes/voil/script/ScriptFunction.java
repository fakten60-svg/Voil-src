package lol.lwes.voil.script;

import java.util.List;

public final class ScriptFunction {
    public final List<String> parameters;
    public final List<ScriptStatement> body;

    public ScriptFunction(List<String> parameters, List<ScriptStatement> body) {
        this.parameters = parameters;
        this.body = body;
    }

    public int arity() {
        return this.parameters.size();
    }
}
