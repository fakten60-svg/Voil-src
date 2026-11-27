package lol.lwes.voil.script.parse;

public record Token(TokenType type, String text, Object value, int line) {
    public boolean is(TokenType type, String text) {
        return this.type == type && this.text.equals(text);
    }

    public boolean isSymbol(String symbol) {
        return this.is(TokenType.SYMBOL, symbol);
    }

    public boolean isKeyword(String keyword) {
        return this.is(TokenType.KEYWORD, keyword);
    }

    @Override
    public String toString() {
        return this.type == TokenType.EOF ? "end of script" : "'" + this.text + "'";
    }
}
