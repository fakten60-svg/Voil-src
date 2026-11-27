package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.ClientPlayerHooks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerMixin {
    @Inject(method = "isSneaking", at = @At("HEAD"), cancellable = true)
    private void onIsSneaking(CallbackInfoReturnable callbackinforeturnable) {
        ClientPlayerHooks.onIsSneaking(callbackinforeturnable);
    }

    @Inject(method = "shouldSlowDown", at = @At("HEAD"), cancellable = true)
    private void onShouldSlowDown(CallbackInfoReturnable callbackinforeturnable) {
        ClientPlayerHooks.onShouldSlowDown(callbackinforeturnable);
    }

    @Redirect(
        method = "applyMovementSpeedFactors",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z")
    )
    private boolean applyMovementSpeedFactors(ClientPlayerEntity clientplayerentity) {
        return ClientPlayerHooks.applyMovementSpeedFactors(clientplayerentity);
    }

    @Inject(method = "swingHand", at = @At("HEAD"), cancellable = true)
    public void swingHand(Hand hand, CallbackInfo callbackinfo) {
        ClientPlayerHooks.swingHand(hand, callbackinfo);
    }

    @Inject(
        method = "move",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"
        ),
        cancellable = true
    )
    public void onMoveHook(MovementType movementtype, Vec3d vec3d, CallbackInfo callbackinfo) {
        ClientPlayerHooks.onMoveHook(this, movementtype, vec3d, callbackinfo);
    }
}
