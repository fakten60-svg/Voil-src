package lol.lwes.voil.util.handler;

import java.util.function.Function;
import lol.lwes.voil.event.HotbarKeyEvent;
import lol.lwes.voil.event.UseKeyEvent;

public abstract class UseHotbarEventHandler implements UseEventHandler, HotbarEventHandler {
    private final Function useHandlerFunction;
    private final Function hotbarHandlerFunction;

    public UseHotbarEventHandler(Function function, Function function1) {
        this.useHandlerFunction = function;
        this.hotbarHandlerFunction = function1;
    }

    @Override
    public boolean hasUseHandler() {
        return this.useHandlerFunction != null;
    }

    public UseEventHandler getUseHandlerByHotbar(HotbarKeyEvent hotbarkeyevent) {
        return (UseEventHandler)this.hotbarHandlerFunction.apply(hotbarkeyevent);
    }

    @Override
    public boolean hasHotbarHandler() {
        return this.hotbarHandlerFunction != null;
    }

    public HotbarEventHandler getHotbarHandler2(HotbarKeyEvent hotbarkeyevent) {
        return (HotbarEventHandler)this.getUseHandlerByHotbar(hotbarkeyevent);
    }

    public UseEventHandler getUseHandler2(UseKeyEvent usekeyevent) {
        return (UseEventHandler)this.useHandlerFunction.apply(usekeyevent);
    }

    @Override
    public UseEventHandler getUseHandler(UseKeyEvent usekeyevent) {
        return this.getUseHandler2(usekeyevent);
    }

    @Override
    public HotbarEventHandler getHotbarHandler(HotbarKeyEvent hotbarkeyevent) {
        return this.getHotbarHandler2(hotbarkeyevent);
    }
}
