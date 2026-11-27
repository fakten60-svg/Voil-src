package lol.lwes.voil.script;

import java.util.List;

public class ScriptArgs {
    private final List<ScriptValue> values;

    public ScriptArgs(List<ScriptValue> values) {
        this.values = values;
    }

    public int size() {
        return this.values.size();
    }

    public ScriptValue get(int index) {
        return this.values.get(index);
    }
}
