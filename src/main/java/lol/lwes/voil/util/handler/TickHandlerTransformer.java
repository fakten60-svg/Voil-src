package lol.lwes.voil.util.handler;

import lol.lwes.voil.event.TaskExecuteEvent;

public class TickHandlerTransformer implements HandlerTransformer {
    public static final TickHandlerTransformer INSTANCE = new TickHandlerTransformer();

    @Override
    public HandlerNode transform(HandlerNode node, Object event) {
        return ((TickEventHandler) node).getTickHandler((TaskExecuteEvent) event);
    }

    @Override
    public boolean canTransform(HandlerNode node) {
        return node instanceof TickEventHandler handler && handler.hasTickHandler();
    }
}
