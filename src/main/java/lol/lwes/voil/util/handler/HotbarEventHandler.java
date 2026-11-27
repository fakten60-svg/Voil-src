package lol.lwes.voil.util.handler;

import lol.lwes.voil.event.HotbarKeyEvent;

public interface HotbarEventHandler extends HandlerNode {
    default HotbarEventHandler applyHotbar(HotbarKeyEvent hotbarkeyevent) {
        HotbarHandlerTransformer hotbarhandlertransformer = HotbarHandlerTransformer.INSTANCE;
        return (HotbarEventHandler)this.applyTransformer(hotbarhandlertransformer, hotbarkeyevent, false);
    }

    default HotbarEventHandler getDefaultHotbarHandler() {
        return null;
    }

    default HotbarEventHandler getHotbarHandler(HotbarKeyEvent hotbarkeyevent) {
        return this.getDefaultHotbarHandler();
    }

    boolean hasHotbarHandler();
}
