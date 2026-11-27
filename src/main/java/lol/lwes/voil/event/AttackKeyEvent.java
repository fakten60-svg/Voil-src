package lol.lwes.voil.event;

import lol.lwes.voil.VoilClient;

public class AttackKeyEvent extends KeyBindingEvent {
    public AttackKeyEvent() {
        super(VoilClient.mc.options.attackKey);
    }

    public static int getDefaultPriority() {
        return 110;
    }

    public static int getEventId() {
        return 0;
    }
}
