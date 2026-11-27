package lol.lwes.voil.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import lol.lwes.voil.hook.LivingEntityHook;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @ModifyExpressionValue(
        method = "calcGlidingVelocity",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;")
    )
    private Vec3d setElytraDirection(Vec3d vec3d) {
        return LivingEntityHook.overrideRotationVector(this, vec3d);
    }

    @ModifyExpressionValue(method = "turnHead", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"))
    private float turnHeadInject(float f) {
        return LivingEntityHook.overrideBodyYaw(this, f);
    }

    @Inject(method = "onDamaged", at = @At("HEAD"), cancellable = true)
    public void onEntityDamage(DamageSource damagesource, CallbackInfo callbackinfo) {
        LivingEntityHook.onDamage(this, damagesource);
    }

    @Inject(method = "getHandSwingDuration", at = @At("RETURN"), cancellable = true)
    private void noDelaySwing(CallbackInfoReturnable callbackinforeturnable) {
        LivingEntityHook.onGetHandSwingDuration(this, callbackinforeturnable);
    }

    @ModifyExpressionValue(
        method = "tick",
        at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F", ordinal = 0),
            @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F", ordinal = 1)
        }
    )
    private float tickInject(float f) {
        return LivingEntityHook.overrideHeadYaw(this, f);
    }
}
