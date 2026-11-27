package lol.lwes.voil.script;

import java.util.List;

public class Script {
    public final String name;
    public final List<ScriptStatement> sources;
    public boolean enabled;
    public ScriptInterpreter runtime;

    public Script(String name, List<ScriptStatement> sources) {
        this.name = name;
        this.sources = sources;
        this.enabled = false;
    }

    public ScriptInterpreter getRuntime() {
        return this.runtime;
    }
}
