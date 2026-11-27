package lol.lwes.voil.script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import lol.lwes.voil.VoilClient;
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
import lol.lwes.voil.util.ChatUtil;

public class ScriptInterpreter {
    private static final int MAX_CALL_DEPTH = 256;
    private static final int MAX_LOOP_ITERATIONS = 1_000_000;

    public final Map<String, ScriptNativeFunction> nativeFunctions = new HashMap<>();
    public final Map<String, ScriptFunction> userFunctions = new HashMap<>();
    private final Map<String, List<ScriptFunction>> eventHandlers = new HashMap<>();
    private ScriptScope globalScope = new ScriptScope();
    private ScriptScope currentScope = this.globalScope;
    private int callDepth;

    public void registerNative(String name, ScriptNativeFunction function) {
        this.nativeFunctions.put(name, function);
    }

    public void copyFrom(ScriptInterpreter other) {
        this.nativeFunctions.putAll(other.nativeFunctions);
    }

    public void run(List<ScriptStatement> statements) {
        this.reset();

        try {
            for (ScriptStatement statement : statements) {
                if (statement instanceof ScriptFunctionStmt function) {
                    this.declare(function);
                }
            }

            for (ScriptStatement statement : statements) {
                if (!(statement instanceof ScriptFunctionStmt)) {
                    this.execute(statement);
                }
            }
        } catch (ReturnSignal ignored) {
        } catch (RuntimeException exception) {
            this.reportError(exception);
        }
    }

    public void runEventHandlers(String event) {
        List<ScriptFunction> handlers = this.eventHandlers.get(event);
        if (handlers == null || handlers.isEmpty()) {
            return;
        }

        try {
            for (ScriptFunction handler : List.copyOf(handlers)) {
                this.invokeFunction(event, handler, List.of());
            }
        } catch (RuntimeException exception) {
            this.reportError(exception);
        }
    }

    public ScriptValue callFunction(String name, List<ScriptValue> arguments) {
        ScriptFunction function = this.userFunctions.get(name);
        if (function != null) {
            return this.invokeFunction(name, function, arguments);
        }

        ScriptNativeFunction nativeFunction = this.nativeFunctions.get(name);
        if (nativeFunction != null) {
            return this.invokeNative(name, nativeFunction, arguments);
        }

        throw new ScriptError("Function not found: " + name);
    }

    public List<String> suggestFunctions(String prefix) {
        String lowerPrefix = prefix.toLowerCase();
        Set<String> matches = new TreeSet<>();
        addMatches(this.userFunctions.keySet(), lowerPrefix, matches);
        addMatches(this.nativeFunctions.keySet(), lowerPrefix, matches);
        return new ArrayList<>(matches);
    }

    public List<String> suggestVariables(String prefix) {
        String lowerPrefix = prefix.toLowerCase();
        Set<String> matches = new TreeSet<>();

        for (ScriptScope scope = this.currentScope; scope != null; scope = scope.getParent()) {
            addMatches(scope.getNames(), lowerPrefix, matches);
        }

        return new ArrayList<>(matches);
    }

    private static void addMatches(Collection<String> names, String lowerPrefix, Set<String> matches) {
        for (String name : names) {
            if (name.toLowerCase().startsWith(lowerPrefix)) {
                matches.add(name);
            }
        }
    }

    private void reset() {
        this.userFunctions.clear();
        this.eventHandlers.clear();
        this.globalScope = new ScriptScope();
        this.currentScope = this.globalScope;
        this.callDepth = 0;
    }

    private void reportError(RuntimeException exception) {
        String message = exception instanceof ScriptError error ? error.describe() : describeException(exception);
        ChatUtil.info("[Script Error] " + message);
        VoilClient.getScriptManager().disableScripts();
    }

    private static String describeException(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }

    private void declare(ScriptFunctionStmt function) {
        if (function instanceof EventHandlerStatement handler) {
            this.eventHandlers.computeIfAbsent(handler.getEvent(), event -> new ArrayList<>()).add(new ScriptFunction(List.of(), handler.body));
        } else {
            this.userFunctions.put(function.name, new ScriptFunction(function.params, function.body));
        }
    }

    private void execute(ScriptStatement statement) {
        if (statement instanceof ScriptVarStmt variable) {
            this.currentScope.define(variable.name, this.evaluate(variable.value));
        } else if (statement instanceof ScriptExprStmt expression) {
            this.evaluate(expression.expression);
        } else if (statement instanceof ScriptFunctionStmt function) {
            this.declare(function);
        } else if (statement instanceof IfStatement ifStatement) {
            if (this.isTruthy(this.evaluate(ifStatement.condition))) {
                this.executeBlock(ifStatement.thenBranch, new ScriptScope(this.currentScope));
            } else if (ifStatement.hasElse()) {
                this.executeBlock(ifStatement.elseBranch, new ScriptScope(this.currentScope));
            }
        } else if (statement instanceof WhileStatement whileStatement) {
            int iterations = 0;

            while (this.isTruthy(this.evaluate(whileStatement.condition))) {
                if (++iterations > MAX_LOOP_ITERATIONS) {
                    throw new ScriptError("Loop exceeded " + MAX_LOOP_ITERATIONS + " iterations", whileStatement.condition.getLine());
                }

                this.executeBlock(whileStatement.body, new ScriptScope(this.currentScope));
            }
        } else if (statement instanceof ReturnStatement returnStatement) {
            throw new ReturnSignal(this.evaluate(returnStatement.value));
        } else {
            throw new ScriptError("Unknown statement type: " + statement.getClass().getSimpleName());
        }
    }

    private void executeBlock(List<ScriptStatement> statements, ScriptScope scope) {
        ScriptScope previous = this.currentScope;
        this.currentScope = scope;

        try {
            for (ScriptStatement statement : statements) {
                this.execute(statement);
            }
        } finally {
            this.currentScope = previous;
        }
    }

    private ScriptValue evaluate(Expression expression) {
        if (expression instanceof LiteralExpression literal) {
            return ScriptValue.of(literal.value);
        }

        if (expression instanceof VariableExpression variable) {
            ScriptValue value = this.currentScope.lookup(variable.name);
            if (value == null) {
                throw new ScriptError("Undefined variable '" + variable.name + "'", expression.getLine());
            }

            return value;
        }

        if (expression instanceof AssignExpression assign) {
            ScriptValue value = this.evaluate(assign.value);
            if (!this.currentScope.assign(assign.name, value)) {
                throw new ScriptError("Undefined variable '" + assign.name + "'", expression.getLine());
            }

            return value;
        }

        if (expression instanceof UnaryExpression unary) {
            return this.applyUnary(unary.operator, this.evaluate(unary.operand), expression.getLine());
        }

        if (expression instanceof BinaryExpression binary) {
            return this.evaluateBinary(binary);
        }

        if (expression instanceof CallExpression call) {
            return this.evaluateCall(call);
        }

        if (expression instanceof ListExpression list) {
            List<Object> values = new ArrayList<>(list.elements.size());
            for (Expression element : list.elements) {
                values.add(this.evaluate(element).getRaw());
            }

            return ScriptValue.of(values);
        }

        if (expression instanceof IndexExpression index) {
            return this.evaluateIndex(index);
        }

        throw new ScriptError("Unknown expression type: " + expression.getClass().getSimpleName(), expression.getLine());
    }

    private ScriptValue evaluateCall(CallExpression call) {
        List<ScriptValue> arguments = new ArrayList<>(call.arguments.size());
        for (Expression argument : call.arguments) {
            arguments.add(this.evaluate(argument));
        }

        try {
            return this.callFunction(call.name, arguments);
        } catch (ScriptError error) {
            throw error.atLine(call.getLine());
        }
    }

    private ScriptValue invokeFunction(String name, ScriptFunction function, List<ScriptValue> arguments) {
        if (arguments.size() != function.arity()) {
            throw new ScriptError("Function '" + name + "' expects " + function.arity() + " argument(s) but got " + arguments.size());
        }

        if (this.callDepth >= MAX_CALL_DEPTH) {
            throw new ScriptError("Maximum call depth exceeded in '" + name + "'");
        }

        ScriptScope scope = new ScriptScope(this.globalScope);
        for (int i = 0; i < function.arity(); i++) {
            scope.define(function.parameters.get(i), arguments.get(i));
        }

        this.callDepth++;

        try {
            this.executeBlock(function.body, scope);
        } catch (ReturnSignal signal) {
            return signal.value;
        } finally {
            this.callDepth--;
        }

        return ScriptValue.nullValue();
    }

    private ScriptValue invokeNative(String name, ScriptNativeFunction function, List<ScriptValue> arguments) {
        try {
            ScriptValue result = function.call(new ScriptArgs(arguments));
            return result == null ? ScriptValue.nullValue() : result;
        } catch (ScriptError error) {
            throw error;
        } catch (RuntimeException exception) {
            throw new ScriptError(name + "(): " + describeException(exception));
        }
    }

    private ScriptValue evaluateIndex(IndexExpression expression) {
        ScriptValue target = this.evaluate(expression.target);
        ScriptValue index = this.evaluate(expression.index);
        int line = expression.getLine();
        if (!(index.getRaw() instanceof Number number) || number.doubleValue() != Math.floor(number.doubleValue())) {
            throw new ScriptError("Index must be a whole number but got " + typeName(index.getRaw()), line);
        }

        int position = number.intValue();
        if (target.getRaw() instanceof List<?> list) {
            if (position < 0 || position >= list.size()) {
                throw new ScriptError("Index " + position + " out of bounds for list of size " + list.size(), line);
            }

            return ScriptValue.of(list.get(position));
        }

        if (target.getRaw() instanceof String text) {
            if (position < 0 || position >= text.length()) {
                throw new ScriptError("Index " + position + " out of bounds for string of length " + text.length(), line);
            }

            return ScriptValue.of(String.valueOf(text.charAt(position)));
        }

        throw new ScriptError("Cannot index into " + typeName(target.getRaw()), line);
    }

    private ScriptValue applyUnary(String operator, ScriptValue operand, int line) {
        if (operator.equals("!")) {
            return ScriptValue.of(!this.isTruthy(operand));
        }

        if (operator.equals("-")) {
            Object value = operand.getRaw();
            if (value instanceof Integer integer) {
                return ScriptValue.of(-integer);
            }

            if (value instanceof Number number) {
                return ScriptValue.of(-number.doubleValue());
            }

            throw new ScriptError("Cannot negate " + typeName(value), line);
        }

        throw new ScriptError("Unknown unary operator '" + operator + "'", line);
    }

    private ScriptValue evaluateBinary(BinaryExpression expression) {
        String operator = expression.operator;
        if (operator.equals("&&")) {
            return ScriptValue.of(this.isTruthy(this.evaluate(expression.left)) && this.isTruthy(this.evaluate(expression.right)));
        }

        if (operator.equals("||")) {
            return ScriptValue.of(this.isTruthy(this.evaluate(expression.left)) || this.isTruthy(this.evaluate(expression.right)));
        }

        ScriptValue left = this.evaluate(expression.left);
        ScriptValue right = this.evaluate(expression.right);
        return this.applyBinary(left, operator, right, expression.getLine());
    }

    private ScriptValue applyBinary(ScriptValue left, String operator, ScriptValue right, int line) {
        Object a = left.getRaw();
        Object b = right.getRaw();

        switch (operator) {
            case "+":
                if (a instanceof String || b instanceof String) {
                    return ScriptValue.of(String.valueOf(a) + b);
                }

                if (a instanceof List<?> first && b instanceof List<?> second) {
                    List<Object> combined = new ArrayList<>(first);
                    combined.addAll(second);
                    return ScriptValue.of(combined);
                }

                return this.arithmetic(a, operator, b, line);
            case "-":
            case "*":
            case "/":
            case "%":
                return this.arithmetic(a, operator, b, line);
            case "==":
                return ScriptValue.of(valuesEqual(a, b));
            case "!=":
                return ScriptValue.of(!valuesEqual(a, b));
            case "<":
            case "<=":
            case ">":
            case ">=":
                int comparison = this.compare(a, operator, b, line);
                return ScriptValue.of(switch (operator) {
                    case "<" -> comparison < 0;
                    case "<=" -> comparison <= 0;
                    case ">" -> comparison > 0;
                    default -> comparison >= 0;
                });
            default:
                throw new ScriptError("Unknown operator '" + operator + "'", line);
        }
    }

    private ScriptValue arithmetic(Object a, String operator, Object b, int line) {
        if (!(a instanceof Number first) || !(b instanceof Number second)) {
            throw new ScriptError("Cannot apply '" + operator + "' to " + typeName(a) + " and " + typeName(b), line);
        }

        if (first instanceof Integer x && second instanceof Integer y) {
            if ((operator.equals("/") || operator.equals("%")) && y == 0) {
                throw new ScriptError("Division by zero", line);
            }

            return ScriptValue.of(switch (operator) {
                case "+" -> x + y;
                case "-" -> x - y;
                case "*" -> x * y;
                case "/" -> x / y;
                default -> x % y;
            });
        }

        double x = first.doubleValue();
        double y = second.doubleValue();
        return ScriptValue.of(switch (operator) {
            case "+" -> x + y;
            case "-" -> x - y;
            case "*" -> x * y;
            case "/" -> x / y;
            default -> x % y;
        });
    }

    private int compare(Object a, String operator, Object b, int line) {
        if (a instanceof Number first && b instanceof Number second) {
            return Double.compare(first.doubleValue(), second.doubleValue());
        }

        if (a instanceof String first && b instanceof String second) {
            return first.compareTo(second);
        }

        throw new ScriptError("Cannot compare " + typeName(a) + " and " + typeName(b) + " with '" + operator + "'", line);
    }

    private static boolean valuesEqual(Object a, Object b) {
        if (a instanceof Number first && b instanceof Number second) {
            return first.doubleValue() == second.doubleValue();
        }

        return Objects.equals(a, b);
    }

    private boolean isTruthy(ScriptValue value) {
        Object raw = value.getRaw();
        if (raw == null) {
            return false;
        }

        if (raw instanceof Boolean bool) {
            return bool;
        }

        if (raw instanceof Number number) {
            return number.doubleValue() != 0.0;
        }

        if (raw instanceof String text) {
            return !text.isEmpty();
        }

        if (raw instanceof Collection<?> collection) {
            return !collection.isEmpty();
        }

        return true;
    }

    private static String typeName(Object value) {
        if (value == null) {
            return "null";
        }

        if (value instanceof Number) {
            return "number";
        }

        if (value instanceof String) {
            return "string";
        }

        if (value instanceof Boolean) {
            return "boolean";
        }

        if (value instanceof List) {
            return "list";
        }

        return value.getClass().getSimpleName();
    }

    private static final class ScriptError extends RuntimeException {
        private int line;

        ScriptError(String message) {
            this(message, 0);
        }

        ScriptError(String message, int line) {
            super(message, null, false, false);
            this.line = line;
        }

        ScriptError atLine(int line) {
            if (this.line <= 0) {
                this.line = line;
            }

            return this;
        }

        String describe() {
            return this.line > 0 ? "Line " + this.line + ": " + this.getMessage() : this.getMessage();
        }
    }

    private static final class ReturnSignal extends RuntimeException {
        final ScriptValue value;

        ReturnSignal(ScriptValue value) {
            super(null, null, false, false);
            this.value = value;
        }
    }
}
