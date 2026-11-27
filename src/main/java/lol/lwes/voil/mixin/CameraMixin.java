package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.ArgsEventHook;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    void updateRot(Args args) {
        ArgsEventHook.onRotationArgs(args);
    }

    @Inject(method = "moveBy", at = @At("HEAD"), cancellable = true)
    void onMoveBy(float f, float f1, float f2, CallbackInfo callbackinfo) {
        ArgsEventHook.onEvent(callbackinfo);
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    void updatePos(Args args) {
        ArgsEventHook.onVelocityArgs(args);
    }
}
