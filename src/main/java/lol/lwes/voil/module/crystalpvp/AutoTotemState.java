package lol.lwes.voil.module.crystalpvp;

import java.util.function.Function;
import lol.lwes.voil.event.TaskExecuteEvent;
import lol.lwes.voil.util.handler.TickEventHandlerBase;

public class AutoTotemState extends TickEventHandlerBase {
    public AutoTotemState(Function<TaskExecuteEvent, AutoTotemState> function) {
        super(function);
    }
}
