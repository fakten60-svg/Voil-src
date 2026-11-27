package lol.lwes.voil.hook;

import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.client.Spoofer;
import net.minecraft.network.listener.ClientCommonPacketListener;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class ResourcePackHooks {
    public static void onApply(ClientCommonPacketListener listener, CallbackInfo callbackInfo) {
        Spoofer spoofer = (Spoofer) ModuleManager.INSTANCE.getModule(Spoofer.class);
        if (spoofer == null || !spoofer.isEnabled()) {
            return;
        }

        if (spoofer.resourcePack.isEnabled()) {
            callbackInfo.cancel();
        }
    }
}
