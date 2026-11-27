package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.TitleScreenHook;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void replaceWithCustom(CallbackInfo callbackinfo) {
        TitleScreenHook.onInit(callbackinfo);
    }
}
