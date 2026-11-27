package lol.lwes.voil.event;

import lol.lwes.voil.VoilClient;

public class HotbarKeyEvent extends KeyBindingEvent {
    private final int slot;

    public HotbarKeyEvent(int i) {
        super(VoilClient.mc.options.hotbarKeys[i]);
        this.slot = i;
    }

    public int getSlot() {
        return this.slot;
    }
}
