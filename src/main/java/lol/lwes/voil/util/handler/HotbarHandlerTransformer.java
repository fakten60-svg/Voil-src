package lol.lwes.voil.util.handler;

import lol.lwes.voil.event.HotbarKeyEvent;

public class HotbarHandlerTransformer implements HandlerTransformer {
    public static final HotbarHandlerTransformer INSTANCE = new HotbarHandlerTransformer();

    @Override
    public HandlerNode transform(HandlerNode handlernode, Object object) {
        HotbarEventHandler hotbareventhandler1 = (HotbarEventHandler)handlernode;
        HotbarKeyEvent hotbarkeyevent = (HotbarKeyEvent)object;
        HotbarEventHandler hotbareventhandler = hotbareventhandler1;
        return this.transformHotbar(hotbareventhandler, hotbarkeyevent);
    }

    public boolean canTransformHotbar(HotbarEventHandler hotbareventhandler) {
        return hotbareventhandler.hasHotbarHandler();
    }

    public HotbarEventHandler transformHotbar(HotbarEventHandler hotbareventhandler, HotbarKeyEvent hotbarkeyevent) {
        return hotbareventhandler.getHotbarHandler(hotbarkeyevent);
    }

    @Override
    public boolean canTransform(HandlerNode handlernode) {
        HotbarEventHandler hotbareventhandler = (HotbarEventHandler)handlernode;
        return this.canTransformHotbar(hotbareventhandler);
    }
}
