package lol.lwes.voil.script.parse;

import java.util.ArrayList;
import java.util.List;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.script.ScriptExprStmt;
import lol.lwes.voil.script.ScriptFunctionStmt;
import lol.lwes.voil.script.ScriptStatement;
import lol.lwes.voil.script.ScriptVarStmt;
import lol.lwes.voil.script.ast.AssignExpression;
import lol.lwes.voil.script.ast.BinaryExpression;
import lol.lwes.voil.script.ast.CallExpression;
import lol.lwes.voil.script.ast.EventHandlerStatement;
import lol.lwes.voil.script.ast.Expression;
import lol.lwes.voil.script.ast.IfStatement;
import lol.lwes.voil.script.ast.IndexExpression;
import lol.lwes.voil.script.ast.ListExpression;
import lol.lwes.voil.script.ast.LiteralExpression;
import lol.lwes.voil.script.ast.ReturnStatement;
import lol.lwes.voil.script.ast.UnaryExpression;
import lol.lwes.voil.script.ast.VariableExpression;
import lol.lwes.voil.script.ast.WhileStatement;

public class ScriptParser {
    private final List<Token> tokens;
    private int current;

    public ScriptParser(List<Token> tokens) {
        this.tokens = new ArrayList<>(tokens);
        if (this.tokens.isEmpty() || this.tokens.get(this.tokens.size() - 1).type() != TokenType.EOF) {
            int line = this.tokens.isEmpty() ? 1 : this.tokens.get(this.tokens.size() - 1).line();
            this.tokens.add(new Token(TokenType.EOF, "", null, line));
        }
    }

    public List<ScriptStatement> parse() {
        this.current = 0;
        List<ScriptStatement> statements = new ArrayList<>();

        try {
            while (!this.isAtEnd()) {
                statements.add(this.statement());
            }
        } catch (ScriptSyntaxException exception) {
            VoilClient.getScriptManager().reportScriptError(exception.getMessage());
            statements.clear();
        }

        return statements;
    }

    private ScriptStatement statement() {
        Token token = this.peek();
        if (token.isKeyword("var") || token.isKeyword("let")) {
            this.advance();
            return this.variableDeclaration();
        }

        if (token.isKeyword("func") || token.isKeyword("function")) {
            this.advance();
            return this.functionDeclaration();
        }

        if (token.isKeyword("on")) {
            this.advance();
            String event = this.expectIdentifier("event name").text();
            return new EventHandlerStatement(event, this.block());
        }

        if (token.isKeyword("if")) {
            this.advance();
            return this.ifStatement();
        }

        if (token.isKeyword("while")) {
            this.advance();
            Expression condition = this.parenthesized();
            return new WhileStatement(condition, this.body());
        }

        if (token.isKeyword("return")) {
            this.advance();
            Expression value = this.isStatementEnd() ? this.literal(null, token.line()) : this.expression();
            this.endStatement();
            return new ReturnStatement(value);
        }

        Expression expression = this.expression();
        this.endStatement();
        return new ScriptExprStmt(expression);
    }

    private ScriptStatement variableDeclaration() {
        Token name = this.expectIdentifier("variable name");
        Expression value;
        if (this.match("=")) {
            value = this.expression();
        } else {
            value = this.literal(null, name.line());
        }

        this.endStatement();
        return new ScriptVarStmt(name.text(), value);
    }

    private ScriptStatement functionDeclaration() {
        String name = this.expectIdentifier("function name").text();
        this.expect("(");
        List<String> parameters = new ArrayList<>();
        if (!this.check(")")) {
            do {
                String parameter = this.expectIdentifier("parameter name").text();
                if (parameters.contains(parameter)) {
                    throw this.error("Duplicate parameter '" + parameter + "'");
                }

                parameters.add(parameter);
            } while (this.match(","));
        }

        this.expect(")");
        return new ScriptFunctionStmt(name, parameters, this.block());
    }

    private ScriptStatement ifStatement() {
        Expression condition = this.parenthesized();
        List<ScriptStatement> thenBranch = this.body();
        List<ScriptStatement> elseBranch = null;
        if (this.peek().isKeyword("else")) {
            this.advance();
            if (this.peek().isKeyword("if")) {
                this.advance();
                elseBranch = new ArrayList<>();
                elseBranch.add(this.ifStatement());
            } else {
                elseBranch = this.body();
            }
        }

        return new IfStatement(condition, thenBranch, elseBranch);
    }

    private List<ScriptStatement> body() {
        if (this.check("{")) {
            return this.block();
        }

        List<ScriptStatement> single = new ArrayList<>();
        single.add(this.statement());
        return single;
    }

    private List<ScriptStatement> block() {
        this.expect("{");
        List<ScriptStatement> statements = new ArrayList<>();

        while (!this.check("}")) {
            if (this.isAtEnd()) {
                throw this.error("Expected '}' before end of script");
            }

            statements.add(this.statement());
        }

        this.advance();
        return statements;
    }

    private Expression parenthesized() {
        this.expect("(");
        Expression expression = this.expression();
        this.expect(")");
        return expression;
    }

    private Expression expression() {
        return this.assignment();
    }

    private Expression assignment() {
        Expression target = this.logicalOr();
        Token token = this.peek();
        if (token.type() == TokenType.SYMBOL && isAssignmentOperator(token.text())) {
            this.advance();
            if (!(target instanceof VariableExpression variable)) {
                throw new ScriptSyntaxException("Invalid assignment target", token.line());
            }

            Expression value = this.assignment();
            if (!token.text().equals("=")) {
                String operator = token.text().substring(0, 1);
                value = new BinaryExpression(target, operator, value).atLine(token.line());
            }

            return new AssignExpression(variable.name, value).atLine(token.line());
        }

        return target;
    }

    private Expression logicalOr() {
        Expression left = this.logicalAnd();

        while (this.check("||") || this.peek().isKeyword("or")) {
            int line = this.advance().line();
            left = new BinaryExpression(left, "||", this.logicalAnd()).atLine(line);
        }

        return left;
    }

    private Expression logicalAnd() {
        Expression left = this.equality();

        while (this.check("&&") || this.peek().isKeyword("and")) {
            int line = this.advance().line();
            left = new BinaryExpression(left, "&&", this.equality()).atLine(line);
        }

        return left;
    }

    private Expression equality() {
        Expression left = this.comparison();

        while (this.check("==") || this.check("!=")) {
            Token operator = this.advance();
            left = new BinaryExpression(left, operator.text(), this.comparison()).atLine(operator.line());
        }

        return left;
    }

    private Expression comparison() {
        Expression left = this.term();

        while (this.check("<") || this.check("<=") || this.check(">") || this.check(">=")) {
            Token operator = this.advance();
            left = new BinaryExpression(left, operator.text(), this.term()).atLine(operator.line());
        }

        return left;
    }

    private Expression term() {
        Expression left = this.factor();

        while (this.check("+") || this.check("-")) {
            Token operator = this.advance();
            left = new BinaryExpression(left, operator.text(), this.factor()).atLine(operator.line());
        }

        return left;
    }

    private Expression factor() {
        Expression left = this.unary();

        while (this.check("*") || this.check("/") || this.check("%")) {
            Token operator = this.advance();
            left = new BinaryExpression(left, operator.text(), this.unary()).atLine(operator.line());
        }

        return left;
    }

    private Expression unary() {
        if (this.check("!") || this.check("-") || this.peek().isKeyword("not")) {
            Token operator = this.advance();
            String symbol = operator.text().equals("-") ? "-" : "!";
            return new UnaryExpression(symbol, this.unary()).atLine(operator.line());
        }

        return this.postfix();
    }

    private Expression postfix() {
        Expression expression = this.primary();

        while (true) {
            Token token = this.peek();
            if (token.isSymbol("(")) {
                if (!(expression instanceof VariableExpression variable)) {
                    throw new ScriptSyntaxException("Only named functions can be called", token.line());
                }

                this.advance();
                List<Expression> arguments = new ArrayList<>();
                if (!this.check(")")) {
                    do {
                        arguments.add(this.expression());
                    } while (this.match(","));
                }

                this.expect(")");
                expression = new CallExpression(variable.name, arguments).atLine(token.line());
            } else if (token.isSymbol("[")) {
                this.advance();
                Expression index = this.expression();
                this.expect("]");
                expression = new IndexExpression(expression, index).atLine(token.line());
            } else if (token.isSymbol("++") || token.isSymbol("--")) {
                if (!(expression instanceof VariableExpression variable)) {
                    throw new ScriptSyntaxException("Invalid increment target", token.line());
                }

                this.advance();
                String operator = token.text().substring(0, 1);
                Expression updated = new BinaryExpression(expression, operator, this.literal(1, token.line())).atLine(token.line());
                expression = new AssignExpression(variable.name, updated).atLine(token.line());
            } else {
                return expression;
            }
        }
    }

    private Expression primary() {
        Token token = this.advance();
        switch (token.type()) {
            case NUMBER:
            case STRING:
                return this.literal(token.value(), token.line());
            case IDENTIFIER:
                return new VariableExpression(token.text()).atLine(token.line());
            case KEYWORD:
                switch (token.text()) {
                    case "true":
                        return this.literal(Boolean.TRUE, token.line());
                    case "false":
                        return this.literal(Boolean.FALSE, token.line());
                    case "null":
                        return this.literal(null, token.line());
                    default:
                        break;
                }
                break;
            case SYMBOL:
                if (token.text().equals("(")) {
                    Expression expression = this.expression();
                    this.expect(")");
                    return expression;
                }

                if (token.text().equals("[")) {
                    List<Expression> elements = new ArrayList<>();
                    if (!this.check("]")) {
                        do {
                            if (this.check("]")) {
                                break;
                            }

                            elements.add(this.expression());
                        } while (this.match(","));
                    }

                    this.expect("]");
                    return new ListExpression(elements).atLine(token.line());
                }
                break;
            default:
                break;
        }

        throw new ScriptSyntaxException("Unexpected " + token, token.line());
    }

    private Expression literal(Object value, int line) {
        return new LiteralExpression(value).atLine(line);
    }

    private static boolean isAssignmentOperator(String symbol) {
        return symbol.equals("=") || symbol.equals("+=") || symbol.equals("-=") || symbol.equals("*=") || symbol.equals("/=") || symbol.equals("%=");
    }

    private boolean isStatementEnd() {
        return this.isAtEnd() || this.check(";") || this.check("}");
    }

    private void endStatement() {
        while (this.match(";")) {
        }
    }

    private Token expectIdentifier(String what) {
        Token token = this.peek();
        if (token.type() != TokenType.IDENTIFIER) {
            throw this.error("Expected " + what + " but found " + token);
        }

        return this.advance();
    }

    private void expect(String symbol) {
        if (!this.match(symbol)) {
            throw this.error("Expected '" + symbol + "' but found " + this.peek());
        }
    }

    private boolean match(String symbol) {
        if (this.check(symbol)) {
            this.advance();
            return true;
        }

        return false;
    }

    private boolean check(String symbol) {
        return this.peek().isSymbol(symbol);
    }

    private Token peek() {
        return this.tokens.get(this.current);
    }

    private Token advance() {
        Token token = this.peek();
        if (token.type() != TokenType.EOF) {
            this.current++;
        }

        return token;
    }

    private boolean isAtEnd() {
        return this.peek().type() == TokenType.EOF;
    }

    private ScriptSyntaxException error(String message) {
        return new ScriptSyntaxException(message, this.peek().line());
    }
}
