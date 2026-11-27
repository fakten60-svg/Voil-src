package lol.lwes.voil.util.handler;

import lol.lwes.voil.event.TaskExecuteEvent;

public interface TickEventHandler extends HandlerNode {
    default TickEventHandler applyTick(TaskExecuteEvent taskexecuteevent) {
        TickHandlerTransformer tickhandlertransformer = TickHandlerTransformer.INSTANCE;
        return (TickEventHandler)this.applyTransformer((HandlerTransformer)tickhandlertransformer, taskexecuteevent, true);
    }

    default TickEventHandler getDefaultTickHandler() {
        return null;
    }

    default TickEventHandler getTickHandler(TaskExecuteEvent taskexecuteevent) {
        return this.getDefaultTickHandler();
    }

    boolean hasTickHandler();
}
