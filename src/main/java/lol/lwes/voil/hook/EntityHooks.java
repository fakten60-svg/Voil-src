package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.mixin.accessor.EntityAccessor;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.combat.Hitboxes;
import lol.lwes.voil.util.entity.FakePlayerEntity;
import lol.lwes.voil.util.rotation.Rotation;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public abstract class EntityHooks {
    private static final String NORMAL_MODE = "Normal";

    public static void invokeFallOnMove(Object object, Vec3d vec3d) {
        Entity entity = (Entity)object;
        if (entity.getEntityWorld().isClient()) {
            BlockPos blockpos = entity.getLandingPos();
            BlockState blockstate = entity.getEntityWorld().getBlockState(blockpos);
            ((EntityAccessor)entity).invokeFall(vec3d.y, entity.isOnGround(), blockstate, blockpos);
        }
    }

    public static void onGetTargetingMargin(CallbackInfoReturnable callbackinforeturnable) {
        Hitboxes hitboxes = (Hitboxes)ModuleManager.INSTANCE.getModule(Hitboxes.class);
        if (hitboxes != null && hitboxes.isEnabled() && Hitboxes.expandMode.is(NORMAL_MODE)) {
            callbackinforeturnable.setReturnValue((float)hitboxes.size.getValue());
        }
    }

    public static void onGetRotationVector(Object object, float f, float f1, CallbackInfoReturnable callbackinforeturnable) {
        if (object == VoilClient.mc.player) {
            if (VoilClient.getRotationManager().isActive()) {
                Rotation rotation = VoilClient.getRotationManager().getTargetRotation();
                if (Math.abs(f1 - VoilClient.mc.player.getYaw()) < 0.1F && Math.abs(f - VoilClient.mc.player.getPitch()) < 0.1F) {
                    callbackinforeturnable.setReturnValue(Vec3d.fromPolar(rotation.getPitchFloat(), rotation.getYawFloat()));
                }
            }
        }
    }

    public static void injectFakeRotation(Object object, CallbackInfoReturnable callbackinforeturnable) {
        Rotation rotation = VoilClient.getRotationManager().getTargetRotation();
        if (object == VoilClient.mc.player && rotation != null && VoilClient.getRotationManager().isActive()) {
            callbackinforeturnable.setReturnValue(VoilClient.mc.player.getRotationVector(rotation.getPitchFloat(), rotation.getYawFloat()));
        }
    }

    public static void hookPush(Entity entity, CallbackInfo callbackinfo) {
        if (entity instanceof FakePlayerEntity) {
            callbackinfo.cancel();
        }
    }
}
