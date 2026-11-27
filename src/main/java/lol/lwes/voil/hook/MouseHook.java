package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.MousePressEvent;
import lol.lwes.voil.event.MouseUpdatePostEvent;
import lol.lwes.voil.event.MouseUpdatePreEvent;
import lol.lwes.voil.setting.KeybindSetting;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class MouseHook {
    public static void onMouseUpdatePre(CallbackInfo callbackinfo, double d0, double d1) {
        MouseUpdatePreEvent mouseupdatepreevent = new MouseUpdatePreEvent(d0, d1);
        VoilClient.eventBus.post(mouseupdatepreevent);
        if (mouseupdatepreevent.isCancelled()) {
            callbackinfo.cancel();
        }
    }

    public static void onMouseUpdatePost(CallbackInfo callbackinfo, double d0, double d1) {
        MouseUpdatePostEvent mouseupdatepostevent = new MouseUpdatePostEvent(d0, d1);
        VoilClient.eventBus.post(mouseupdatepostevent);
        if (mouseupdatepostevent.isCancelled()) {
            callbackinfo.cancel();
        }
    }

    public static void onMouseButton(int i, int j, int k, CallbackInfo callbackinfo) {
        double d0 = VoilClient.mc.getWindow().getScaleFactor();
        double d1 = VoilClient.mc.mouse.getX() / d0;
        double d2 = VoilClient.mc.mouse.getY() / d0;
        if (j == 1) {
            KeybindSetting.addPressedKey(0);
        }

        MousePressEvent mousepressevent = new MousePressEvent(i, j, k, d1, d2);
        VoilClient.eventBus.post(mousepressevent);
        if (mousepressevent.isCancelled()) {
            callbackinfo.cancel();
        }
    }
}
