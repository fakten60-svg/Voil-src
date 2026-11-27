package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.SnapTapKeyHook;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil.Key;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public abstract class KeybindingMixin {
    @Shadow
    @Final
    private Key defaultKey;
    @Shadow
    private boolean pressed;

    @Inject(method = "isPressed", at = @At("HEAD"), cancellable = true)
    public void onGetPressed(CallbackInfoReturnable callbackinforeturnable) {
        SnapTapKeyHook.onIsKeyPressed(callbackinforeturnable, this.defaultKey, this.pressed);
    }

    @Inject(method = "setPressed", at = @At("HEAD"))
    public void setPressed(boolean flag, CallbackInfo callbackinfo) {
        SnapTapKeyHook.onKeyStateChange(flag, this.defaultKey);
    }
}
