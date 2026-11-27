package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.KeyboardHook;
import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    public void onKey(long j, int i, KeyInput keyinput, CallbackInfo callbackinfo) {
        KeyboardHook.onKey(i, keyinput);
    }
}
