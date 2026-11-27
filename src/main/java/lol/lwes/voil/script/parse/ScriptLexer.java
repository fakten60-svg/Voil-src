package lol.lwes.voil.script.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lol.lwes.voil.VoilClient;

public class ScriptLexer {
    private static final Set<String> KEYWORDS = Set.of(
        "var", "let", "func", "function", "on", "if", "else", "while", "return", "true", "false", "null", "and", "or", "not"
    );
    private static final Set<String> TWO_CHAR_SYMBOLS = Set.of(
        "==", "!=", "<=", ">=", "&&", "||", "+=", "-=", "*=", "/=", "%=", "++", "--"
    );
    private static final String SINGLE_CHAR_SYMBOLS = "+-*/%=<>!(){}[],;.";

    private final String source;
    private int position;
    private int line = 1;

    public ScriptLexer(String source) {
        this.source = source == null ? "" : source;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        this.position = 0;
        this.line = 1;

        try {
            while (true) {
                this.skipWhitespaceAndComments();
                if (this.isAtEnd()) {
                    break;
                }

                tokens.add(this.nextToken());
            }
        } catch (ScriptSyntaxException exception) {
            VoilClient.getScriptManager().reportScriptError(exception.getMessage());
        }

        tokens.add(new Token(TokenType.EOF, "", null, this.line));
        return tokens;
    }

    private Token nextToken() {
        char c = this.peek();
        if (Character.isDigit(c) || c == '.' && Character.isDigit(this.peekNext())) {
            return this.readNumber();
        }

        if (c == '"' || c == '\'') {
            return this.readString();
        }

        if (Character.isLetter(c) || c == '_' || c == '$') {
            return this.readWord();
        }

        if (this.position + 1 < this.source.length()) {
            String pair = this.source.substring(this.position, this.position + 2);
            if (TWO_CHAR_SYMBOLS.contains(pair)) {
                this.position += 2;
                return new Token(TokenType.SYMBOL, pair, null, this.line);
            }
        }

        if (SINGLE_CHAR_SYMBOLS.indexOf(c) >= 0) {
            this.position++;
            return new Token(TokenType.SYMBOL, String.valueOf(c), null, this.line);
        }

        throw new ScriptSyntaxException("Unexpected character '" + c + "'", this.line);
    }

    private Token readNumber() {
        int start = this.position;
        boolean decimal = false;

        while (!this.isAtEnd()) {
            char c = this.peek();
            if (Character.isDigit(c) || c == '_') {
                this.position++;
            } else if (c == '.' && !decimal && Character.isDigit(this.peekNext())) {
                decimal = true;
                this.position++;
            } else {
                break;
            }
        }

        if (!this.isAtEnd() && (this.peek() == 'f' || this.peek() == 'F' || this.peek() == 'd' || this.peek() == 'D')) {
            decimal = true;
            this.position++;
        }

        String text = this.source.substring(start, this.position);
        String digits = text.replace("_", "").replaceAll("[fFdD]$", "");
        Object value;
        try {
            if (decimal) {
                value = Double.parseDouble(digits);
            } else {
                long parsed = Long.parseLong(digits);
                value = parsed <= Integer.MAX_VALUE ? (Object)(int)parsed : (Object)(double)parsed;
            }
        } catch (NumberFormatException exception) {
            throw new ScriptSyntaxException("Invalid number '" + text + "'", this.line);
        }

        return new Token(TokenType.NUMBER, text, value, this.line);
    }

    private Token readString() {
        char quote = this.advance();
        int startLine = this.line;
        StringBuilder builder = new StringBuilder();

        while (true) {
            if (this.isAtEnd()) {
                throw new ScriptSyntaxException("Unterminated string", startLine);
            }

            char c = this.advance();
            if (c == quote) {
                break;
            }

            if (c == '\n') {
                this.line++;
            }

            if (c == '\\' && !this.isAtEnd()) {
                char escaped = this.advance();
                switch (escaped) {
                    case 'n' -> builder.append('\n');
                    case 't' -> builder.append('\t');
                    case 'r' -> builder.append('\r');
                    case '0' -> builder.append('\0');
                    default -> builder.append(escaped);
                }
            } else {
                builder.append(c);
            }
        }

        String value = builder.toString();
        return new Token(TokenType.STRING, value, value, startLine);
    }

    private Token readWord() {
        int start = this.position;

        while (!this.isAtEnd()) {
            char c = this.peek();
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '$') {
                break;
            }

            this.position++;
        }

        String word = this.source.substring(start, this.position);
        TokenType type = KEYWORDS.contains(word) ? TokenType.KEYWORD : TokenType.IDENTIFIER;
        return new Token(type, word, null, this.line);
    }

    private void skipWhitespaceAndComments() {
        while (!this.isAtEnd()) {
            char c = this.peek();
            if (c == '\n') {
                this.line++;
                this.position++;
            } else if (Character.isWhitespace(c)) {
                this.position++;
            } else if (c == '#' || c == '/' && this.peekNext() == '/') {
                while (!this.isAtEnd() && this.peek() != '\n') {
                    this.position++;
                }
            } else if (c == '/' && this.peekNext() == '*') {
                int startLine = this.line;
                this.position += 2;

                while (!(this.peek() == '*' && this.peekNext() == '/')) {
                    if (this.isAtEnd()) {
                        throw new ScriptSyntaxException("Unterminated block comment", startLine);
                    }

                    if (this.advance() == '\n') {
                        this.line++;
                    }
                }

                this.position += 2;
            } else {
                break;
            }
        }
    }

    private boolean isAtEnd() {
        return this.position >= this.source.length();
    }

    private char peek() {
        return this.isAtEnd() ? '\0' : this.source.charAt(this.position);
    }

    private char peekNext() {
        return this.position + 1 >= this.source.length() ? '\0' : this.source.charAt(this.position + 1);
    }

    private char advance() {
        return this.source.charAt(this.position++);
    }
}
