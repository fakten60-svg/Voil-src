package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.PacketApplyBatcherHooks;
import net.minecraft.network.PacketApplyBatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketApplyBatcher.class)
public abstract class PacketApplyBatcherMixin {
    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private void onFrameApply(CallbackInfo callbackinfo) {
        PacketApplyBatcherHooks.onFrameApply(callbackinfo);
    }
}
