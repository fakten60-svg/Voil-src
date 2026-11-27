package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.GameRendererHooks;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "updateCrosshairTarget", at = @At("HEAD"), cancellable = true)
    private void onUpdateCrosshairTargetPre(float f, CallbackInfo callbackinfo) {
        GameRendererHooks.onUpdateCrosshairTargetPre(f, callbackinfo);
    }

    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    public void onShowFloatingItem(ItemStack itemstack, CallbackInfo callbackinfo) {
        GameRendererHooks.onShowFloatingItem(callbackinfo);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onEndRender(RenderTickCounter rendertickcounter, boolean flag, CallbackInfo callbackinfo) {
        GameRendererHooks.onEndRender();
    }
}
