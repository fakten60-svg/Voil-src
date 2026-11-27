package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.InventoryScreenHooks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryMixin {
    @Inject(method = "drawForeground", at = @At("HEAD"), cancellable = true)
    private void onDrawForeground(DrawContext drawcontext, int i, int j, CallbackInfo callbackinfo) {
        InventoryScreenHooks.onDrawForeground(drawcontext, i, j, callbackinfo);
    }

    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    private void onDrawBackground(DrawContext drawcontext, float f, int i, int j, CallbackInfo callbackinfo) {
        InventoryScreenHooks.onDrawBackground(this, drawcontext, f, i, j, callbackinfo);
    }
}
