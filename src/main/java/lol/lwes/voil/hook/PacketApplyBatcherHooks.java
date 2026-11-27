package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.EventBus;
import lol.lwes.voil.event.RenderTickEvent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class PacketApplyBatcherHooks {
    public static void onFrameApply(CallbackInfo callbackinfo) {
        EventBus eventbus = VoilClient.eventBus;
        RenderTickEvent rendertickevent = RenderTickEvent.getInstance();
        if (((RenderTickEvent)eventbus.post(rendertickevent)).isCancelled()) {
            callbackinfo.cancel();
        }
    }
}
