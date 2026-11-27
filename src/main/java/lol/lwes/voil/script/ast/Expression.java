package lol.lwes.voil.script.ast;

public abstract class Expression {
    private int line;

    public int getLine() {
        return this.line;
    }

    public Expression atLine(int line) {
        this.line = line;
        return this;
    }
}
