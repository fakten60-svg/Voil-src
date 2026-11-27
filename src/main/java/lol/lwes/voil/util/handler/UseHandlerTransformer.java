package lol.lwes.voil.util.handler;

import lol.lwes.voil.event.UseKeyEvent;

public class UseHandlerTransformer implements HandlerTransformer {
    public static final UseHandlerTransformer INSTANCE = new UseHandlerTransformer();

    @Override
    public HandlerNode transform(HandlerNode handlernode, Object object) {
        UseEventHandler useeventhandler1 = (UseEventHandler)handlernode;
        UseKeyEvent usekeyevent = (UseKeyEvent)object;
        UseEventHandler useeventhandler = useeventhandler1;
        return this.transformUse(useeventhandler, usekeyevent);
    }

    public boolean canTransformUse(UseEventHandler useeventhandler) {
        return useeventhandler.hasUseHandler();
    }

    public UseEventHandler transformUse(UseEventHandler useeventhandler, UseKeyEvent usekeyevent) {
        return useeventhandler.getUseHandler(usekeyevent);
    }

    @Override
    public boolean canTransform(HandlerNode handlernode) {
        UseEventHandler useeventhandler = (UseEventHandler)handlernode;
        return this.canTransformUse(useeventhandler);
    }
}
