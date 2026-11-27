package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.MouseHook;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow
    private double cursorDeltaX;
    @Shadow
    private double cursorDeltaY;

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long j, MouseInput mouseinput, int i, CallbackInfo callbackinfo) {
        MouseHook.onMouseButton(mouseinput.button(), i, mouseinput.modifiers(), callbackinfo);
    }

    @Inject(method = "updateMouse", at = @At("RETURN"), cancellable = true)
    private void onUpdateMousePost(CallbackInfo callbackinfo) {
        MouseHook.onMouseUpdatePost(callbackinfo, this.cursorDeltaX, this.cursorDeltaY);
    }

    @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
    private void onUpdateMousePre(CallbackInfo callbackinfo) {
        MouseHook.onMouseUpdatePre(callbackinfo, this.cursorDeltaX, this.cursorDeltaY);
    }
}
