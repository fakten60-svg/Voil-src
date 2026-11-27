package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.ScreenHooks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Inject(
        method = "renderInGameBackground",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onRenderInGameBackground(DrawContext drawcontext, CallbackInfo callbackinfo) {
        ScreenHooks.onRenderInGameBackground(this, callbackinfo);
    }
}
