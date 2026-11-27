package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.EntityHooks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "getTargetingMargin", at = @At("HEAD"), cancellable = true)
    private void onGetTargetingMargin(CallbackInfoReturnable callbackinforeturnable) {
        EntityHooks.onGetTargetingMargin(callbackinforeturnable);
    }

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    private void hookPush(Entity entity, CallbackInfo callbackinfo) {
        EntityHooks.hookPush(entity, callbackinfo);
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isRemoved()Z"))
    public void invokeFallOnMove(MovementType movementtype, Vec3d vec3d, CallbackInfo callbackinfo) {
        EntityHooks.invokeFallOnMove(this, vec3d);
    }

    @Inject(method = "getRotationVec", at = @At("HEAD"), cancellable = true)
    public void injectFakeRotation(float f, CallbackInfoReturnable callbackinforeturnable) {
        EntityHooks.injectFakeRotation(this, callbackinforeturnable);
    }

    @Inject(method = "getRotationVector(FF)Lnet/minecraft/util/math/Vec3d;", at = @At("HEAD"), cancellable = true)
    private void onGetRotationVector(float f, float f1, CallbackInfoReturnable callbackinforeturnable) {
        EntityHooks.onGetRotationVector(this, f, f1, callbackinforeturnable);
    }
}
