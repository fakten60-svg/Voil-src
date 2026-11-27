package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.ClientPlayerInteractionManagerHooks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Inject(method = "interactBlock", at = @At("RETURN"), cancellable = true)
    private void onRightClickBlock(
        ClientPlayerEntity clientplayerentity, Hand hand, BlockHitResult blockhitresult, CallbackInfoReturnable callbackinforeturnable
    ) {
        ClientPlayerInteractionManagerHooks.onRightClickBlock(hand, blockhitresult, callbackinforeturnable);
    }

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void preAttackEntity(PlayerEntity playerentity, Entity entity, CallbackInfo callbackinfo) {
        ClientPlayerInteractionManagerHooks.preAttackEntity(playerentity, entity, callbackinfo);
    }

    @Inject(method = "attackEntity", at = @At("RETURN"))
    private void postAttackEntity(PlayerEntity playerentity, Entity entity, CallbackInfo callbackinfo) {
        ClientPlayerInteractionManagerHooks.postAttackEntity(playerentity, entity);
    }

    @Inject(
        method = "interactItem",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;gameMode:Lnet/minecraft/world/GameMode;")
    )
    private void sendInteractItemMovementPacketsPre(PlayerEntity playerentity, Hand hand, CallbackInfoReturnable callbackinforeturnable) {
        ClientPlayerInteractionManagerHooks.sendInteractItemMovementPacketsPre(playerentity);
    }

    @Inject(method = "interactItem", at = @At("RETURN"))
    private void sendInteractItemMovementPacketsPost(PlayerEntity playerentity, Hand hand, CallbackInfoReturnable callbackinforeturnable) {
        ClientPlayerInteractionManagerHooks.sendInteractItemMovementPacketsPost(playerentity);
    }
}
