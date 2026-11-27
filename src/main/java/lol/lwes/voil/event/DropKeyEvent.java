package lol.lwes.voil.event;

import lol.lwes.voil.VoilClient;

public class DropKeyEvent extends KeyBindingEvent {
    public DropKeyEvent() {
        super(VoilClient.mc.options.inventoryKey);
    }
}
