package lol.lwes.voil.util.handler;

import java.util.function.Function;
import lol.lwes.voil.event.AttackKeyEvent;
import lol.lwes.voil.event.HotbarKeyEvent;

public abstract class AttackHotbarEventHandler implements AttackEventHandler, HotbarEventHandler {
    private final Function attackHandlerProvider;
    private final Function hotbarHandlerProvider;

    public AttackHotbarEventHandler(Function function, Function function1) {
        this.attackHandlerProvider = function;
        this.hotbarHandlerProvider = function1;
    }

    @Override
    public boolean hasAttackHandler() {
        return this.attackHandlerProvider != null;
    }

    @Override
    public boolean hasHotbarHandler() {
        return this.hotbarHandlerProvider != null;
    }

    public AttackEventHandler getAttackHandler2(AttackKeyEvent attackkeyevent) {
        return (AttackEventHandler)this.attackHandlerProvider.apply(attackkeyevent);
    }

    public HotbarEventHandler getHotbarHandler2(HotbarKeyEvent hotbarkeyevent) {
        return (HotbarEventHandler)this.getAttackHandlerByHotbar(hotbarkeyevent);
    }

    public AttackEventHandler getAttackHandlerByHotbar(HotbarKeyEvent hotbarkeyevent) {
        return (AttackEventHandler)this.hotbarHandlerProvider.apply(hotbarkeyevent);
    }

    @Override
    public HotbarEventHandler getHotbarHandler(HotbarKeyEvent hotbarkeyevent) {
        return this.getHotbarHandler2(hotbarkeyevent);
    }

    @Override
    public AttackEventHandler getAttackHandler(AttackKeyEvent attackkeyevent) {
        return this.getAttackHandler2(attackkeyevent);
    }
}
