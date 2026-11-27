package lol.lwes.voil.script;

import java.util.List;

public class ScriptModuleInterpreter extends ScriptInterpreter {
    final Script module;

    ScriptModuleInterpreter(Script module) {
        this.module = module;
    }

    @Override
    public ScriptValue callFunction(String name, List<ScriptValue> arguments) {
        return !this.module.enabled ? ScriptValue.nullValue() : super.callFunction(name, arguments);
    }

    @Override
    public void runEventHandlers(String event) {
        if (this.module.enabled) {
            super.runEventHandlers(event);
        }
    }
}
