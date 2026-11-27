package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.AttackEntityPostEvent;
import lol.lwes.voil.event.AttackEntityPreEvent;
import lol.lwes.voil.event.BlockInteractEvent;
import lol.lwes.voil.event.EventBus;
import lol.lwes.voil.event.HookPostEvent;
import lol.lwes.voil.event.HookPreEvent;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class ClientPlayerInteractionManagerHooks {
    public static void postAttackEntity(PlayerEntity playerentity, Entity entity) {
        EventBus eventbus = VoilClient.eventBus;
        AttackEntityPostEvent attackentitypostevent = AttackEntityPostEvent.create(playerentity, entity);
        eventbus.post(attackentitypostevent);
    }

    public static void sendInteractItemMovementPacketsPost(PlayerEntity playerentity) {
        if (VoilClient.mc.player == playerentity) {
            VoilClient.eventBus.postObject(HookPreEvent.create("interactItem"));
        }
    }

    public static void sendInteractItemMovementPacketsPre(PlayerEntity playerentity) {
        if (VoilClient.mc.player == playerentity) {
            VoilClient.eventBus.postObject(HookPostEvent.create("interactItem"));
        }
    }

    public static void onRightClickBlock(Hand hand, BlockHitResult blockhitresult, CallbackInfoReturnable callbackinforeturnable) {
        BlockPos blockpos = blockhitresult.getBlockPos();
        Block block = MinecraftClient.getInstance().world.getBlockState(blockpos).getBlock();
        BlockInteractEvent blockinteractevent = new BlockInteractEvent(block, hand, blockpos, blockpos.getX(), blockpos.getY(), blockpos.getZ());
        VoilClient.eventBus.post(blockinteractevent);
        if (blockinteractevent.isCancelled()) {
            callbackinforeturnable.setReturnValue(ActionResult.FAIL);
        }
    }

    public static void preAttackEntity(PlayerEntity playerentity, Entity entity, CallbackInfo callbackinfo) {
        EventBus eventbus = VoilClient.eventBus;
        AttackEntityPreEvent attackentitypreevent = AttackEntityPreEvent.create(playerentity, entity);
        if (((AttackEntityPreEvent)eventbus.post(attackentitypreevent)).isCancelled()) {
            callbackinfo.cancel();
        }
    }
}
