package lol.lwes.voil.script.parse;

public class ScriptSyntaxException extends RuntimeException {
    private final int line;

    public ScriptSyntaxException(String message, int line) {
        super("Line " + line + ": " + message);
        this.line = line;
    }

    public int getLine() {
        return this.line;
    }
}
