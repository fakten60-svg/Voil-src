package lol.lwes.voil.script;

@FunctionalInterface
public interface ScriptNativeFunction {
    ScriptValue call(ScriptArgs args);
}
