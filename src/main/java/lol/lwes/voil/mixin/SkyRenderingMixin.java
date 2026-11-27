package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.RenderLayerHook;
import net.minecraft.client.render.SkyRendering;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRendering.class)
public abstract class SkyRenderingMixin {
    @Inject(method = "renderTopSky", at = @At("HEAD"), cancellable = true)
    private void onRenderTopSky(CallbackInfo callbackinfo) {
        RenderLayerHook.cancelTopSky(callbackinfo);
    }

    @Inject(method = "renderGlowingSky", at = @At("HEAD"), cancellable = true)
    private void onRenderGlowingSky(MatrixStack matrixstack, float f, int i, CallbackInfo callbackinfo) {
        RenderLayerHook.cancelGlowingSky(callbackinfo);
    }

    @Inject(method = "renderSkyDark", at = @At("HEAD"), cancellable = true)
    private void onRenderSkyDark(CallbackInfo callbackinfo) {
        RenderLayerHook.cancelSkyDark(callbackinfo);
    }

    @Inject(method = "renderCelestialBodies", at = @At("HEAD"), cancellable = true)
    private void onRenderCelestialBodies(CallbackInfo callbackinfo) {
        RenderLayerHook.cancelCelestialBodies(callbackinfo);
    }
}
