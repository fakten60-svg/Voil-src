package lol.lwes.voil.script.ast;

import java.util.List;

public final class CallExpression extends Expression {
    public final String name;
    public final List<Expression> arguments;

    public CallExpression(String name, List<Expression> arguments) {
        this.name = name;
        this.arguments = List.copyOf(arguments);
    }
}
