package lol.lwes.voil.hook;

import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.client.OBSBypass;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class ObsBypassHook {
    public static void onCapture(CallbackInfo callbackinfo) {
        OBSBypass obsbypass = (OBSBypass)ModuleManager.INSTANCE.getModule(OBSBypass.class);
        if (obsbypass != null && obsbypass.isEnabled()) {
            obsbypass.applyBorderlessFullscreen(callbackinfo, false);
        }
    }
}
