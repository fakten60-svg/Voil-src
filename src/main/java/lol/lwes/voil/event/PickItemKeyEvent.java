package lol.lwes.voil.event;

import lol.lwes.voil.VoilClient;

public class PickItemKeyEvent extends KeyBindingEvent {
    public PickItemKeyEvent() {
        super(VoilClient.mc.options.pickItemKey);
    }
}
