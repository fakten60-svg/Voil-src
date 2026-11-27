package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.SilentRotationHook;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(
        method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
        at = @At("HEAD")
    )
    private void onUpdateRenderStateRestore(LivingEntity livingentity, LivingEntityRenderState livingentityrenderstate, float f, CallbackInfo callbackinfo) {
        SilentRotationHook.restoreRotation(livingentity);
    }

    @Inject(
        method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
        at = @At("HEAD")
    )
    private void onUpdateRenderStateApply(LivingEntity livingentity, LivingEntityRenderState livingentityrenderstate, float f, CallbackInfo callbackinfo) {
        SilentRotationHook.applyRotation(livingentity);
    }
}
