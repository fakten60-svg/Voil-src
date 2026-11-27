package lol.lwes.voil.script.ast;

import java.util.List;
import lol.lwes.voil.script.ScriptFunctionStmt;
import lol.lwes.voil.script.ScriptStatement;

public final class EventHandlerStatement extends ScriptFunctionStmt {
    public EventHandlerStatement(String event, List<ScriptStatement> body) {
        super(event, List.of(), body);
    }

    public String getEvent() {
        return this.name;
    }
}
