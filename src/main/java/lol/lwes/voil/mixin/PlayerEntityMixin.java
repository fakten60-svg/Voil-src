package lol.lwes.voil.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import lol.lwes.voil.hook.PlayerEntityHooks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "getBlockInteractionRange", at = @At("HEAD"), cancellable = true)
    private void hookBlockInteractionRange(CallbackInfoReturnable callbackinforeturnable) {
        PlayerEntityHooks.hookBlockInteractionRange(this, callbackinforeturnable);
    }

    @Inject(method = "getAttackCooldownProgress", at = @At("HEAD"), cancellable = true)
    private void alwaysReady(float f, CallbackInfoReturnable callbackinforeturnable) {
        PlayerEntityHooks.alwaysReady(callbackinforeturnable);
    }

    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getYaw()F"))
    private float injectRotation(float f) {
        return PlayerEntityHooks.injectRotation(this, f);
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void onTravelhookPre(Vec3d vec3d, CallbackInfo callbackinfo) {
        PlayerEntityHooks.onTravelHookPre(vec3d, callbackinfo);
    }

    @Inject(method = "travel", at = @At("RETURN"), cancellable = true)
    private void onTravelhookPost(Vec3d vec3d, CallbackInfo callbackinfo) {
        PlayerEntityHooks.onTravelHookPost(vec3d, callbackinfo);
    }

    @Inject(method = "getPoses", at = @At("HEAD"), cancellable = true)
    private void getBaseDimensions(CallbackInfoReturnable callbackinforeturnable) {
        PlayerEntityHooks.getBaseDimensions(this, callbackinforeturnable);
    }

    @Inject(method = "getEntityInteractionRange", at = @At("HEAD"), cancellable = true)
    private void hookEntityInteractionRange(CallbackInfoReturnable callbackinforeturnable) {
        PlayerEntityHooks.hookEntityInteractionRange(this, callbackinforeturnable);
    }
}
