package lol.lwes.voil.event;

import lol.lwes.voil.VoilClient;

public class UseKeyEvent extends KeyBindingEvent {
    public UseKeyEvent() {
        super(VoilClient.mc.options.useKey);
    }
}
