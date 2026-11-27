package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.FogAlphaHooks;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class FogAlphaMixin {
    @Inject(method = "getFogColor", at = @At("RETURN"), cancellable = true)
    private void onGetFogColor(Camera camera, float f, ClientWorld clientworld, int i, float f1, CallbackInfoReturnable callbackinforeturnable) {
        FogAlphaHooks.onGetFogColor(callbackinforeturnable);
    }
}
