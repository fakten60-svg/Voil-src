package lol.lwes.voil.util.handler;

import java.util.function.Function;
import lol.lwes.voil.event.TaskExecuteEvent;

public abstract class TickEventHandlerBase implements TickEventHandler {
    private final Function tickHandlerFactory;

    public TickEventHandlerBase(Function function) {
        this.tickHandlerFactory = function;
    }

    @Override
    public boolean hasTickHandler() {
        return this.tickHandlerFactory != null;
    }

    public TickEventHandler getTickHandler2(TaskExecuteEvent taskexecuteevent) {
        return (TickEventHandler)this.tickHandlerFactory.apply(taskexecuteevent);
    }

    @Override
    public TickEventHandler getTickHandler(TaskExecuteEvent taskexecuteevent) {
        return this.getTickHandler2(taskexecuteevent);
    }
}
