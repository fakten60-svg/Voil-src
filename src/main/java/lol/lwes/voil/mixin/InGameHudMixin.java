package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.HudRenderHook;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void onRenderHotbar(DrawContext drawcontext, RenderTickCounter rendertickcounter, CallbackInfo callbackinfo) {
        HudRenderHook.renderHudOverlay(drawcontext, callbackinfo);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    void render(DrawContext drawcontext, RenderTickCounter rendertickcounter, CallbackInfo callbackinfo) {
        HudRenderHook.onRenderHud(drawcontext, callbackinfo);
    }
}
