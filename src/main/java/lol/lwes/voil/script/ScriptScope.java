package lol.lwes.voil.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptScope {
    private final Map<String, ScriptValue> variables = new HashMap<>();
    private final ScriptScope parent;

    public ScriptScope() {
        this.parent = null;
    }

    public ScriptScope(ScriptScope parent) {
        this.parent = parent;
    }

    public void define(String name, ScriptValue value) {
        this.variables.put(name, value);
    }

    public ScriptValue lookup(String name) {
        ScriptValue value = this.variables.get(name);
        if (value != null) {
            return value;
        }

        return this.parent != null ? this.parent.lookup(name) : null;
    }

    public boolean assign(String name, ScriptValue value) {
        if (this.variables.containsKey(name)) {
            this.variables.put(name, value);
            return true;
        }

        return this.parent != null && this.parent.assign(name, value);
    }

    public List<String> getNames() {
        return new ArrayList<>(this.variables.keySet());
    }

    public ScriptScope getParent() {
        return this.parent;
    }
}
