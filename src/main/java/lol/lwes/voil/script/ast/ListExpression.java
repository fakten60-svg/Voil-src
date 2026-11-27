package lol.lwes.voil.script.ast;

import java.util.List;

public final class ListExpression extends Expression {
    public final List<Expression> elements;

    public ListExpression(List<Expression> elements) {
        this.elements = List.copyOf(elements);
    }
}
