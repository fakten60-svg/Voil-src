package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.HookPostEvent;
import lol.lwes.voil.event.HookPreEvent;

public class ClientPlayerEntityHooks {
    public static void sendTickMovementPacketsPost(Object object) {
        if (!VoilClient.getInstance().disabled) {
            if (VoilClient.mc.player == object) {
                VoilClient.eventBus.postObject(HookPreEvent.create("tick"));
            }
        }
    }

    public static void sendTickMovementPacketsPre(Object object) {
        if (!VoilClient.getInstance().disabled) {
            if (VoilClient.mc.player == object) {
                VoilClient.eventBus.postObject(HookPostEvent.create("tick"));
            }
        }
    }
}
