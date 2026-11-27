package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.PlayerTravelPostEvent;
import lol.lwes.voil.event.PlayerTravelPreEvent;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.combat.Hitboxes;
import lol.lwes.voil.module.combat.Reach;
import lol.lwes.voil.module.player.NoAttackCooldown;
import lol.lwes.voil.util.rotation.RotationManager;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public abstract class PlayerEntityHooks {
    public static void onTravelHookPost(Vec3d vec3d, CallbackInfo callbackinfo) {
        if (VoilClient.mc.player != null) {
            PlayerTravelPreEvent playertravelpreevent = new PlayerTravelPreEvent(vec3d);
            VoilClient.eventBus.post(playertravelpreevent);
            if (playertravelpreevent.isCancelled()) {
                VoilClient.mc.player.move(MovementType.SELF, VoilClient.mc.player.getVelocity());
                callbackinfo.cancel();
            }
        }
    }

    public static void hookEntityInteractionRange(Object object, CallbackInfoReturnable callbackinforeturnable) {
        VoilClient voilclient = VoilClient.getInstance();
        if (!voilclient.disabled) {
            if (VoilClient.mc.player == object) {
                ModuleManager modulemanager = ModuleManager.INSTANCE;
                Reach reach = (Reach)modulemanager.getModule(Reach.class);
                if (reach != null && reach.isEnabled() && reach.mode.isSelected("Entity Interactions")) {
                    callbackinforeturnable.setReturnValue(reach.entityReach.getValue());
                }
            }
        }
    }

    public static void hookBlockInteractionRange(Object object, CallbackInfoReturnable callbackinforeturnable) {
        VoilClient voilclient = VoilClient.getInstance();
        if (!voilclient.disabled) {
            if (VoilClient.mc.player == object) {
                ModuleManager modulemanager = ModuleManager.INSTANCE;
                Reach reach = (Reach)modulemanager.getModule(Reach.class);
                if (reach != null && reach.isEnabled() && reach.mode.isSelected("Block Interactions")) {
                    callbackinforeturnable.setReturnValue(reach.blockReach.getValue());
                }
            }
        }
    }

    public static void alwaysReady(CallbackInfoReturnable callbackinforeturnable) {
        if (ModuleManager.INSTANCE.isEnabled(NoAttackCooldown.class) && VoilClient.mc.player != null) {
            callbackinforeturnable.setReturnValue(1.0F);
        }
    }

    public static void onTravelHookPre(Vec3d vec3d, CallbackInfo callbackinfo) {
        if (VoilClient.mc.player != null) {
            PlayerTravelPostEvent playertravelpostevent = new PlayerTravelPostEvent(vec3d);
            VoilClient.eventBus.post(playertravelpostevent);
            if (playertravelpostevent.isCancelled()) {
                VoilClient.mc.player.move(MovementType.SELF, VoilClient.mc.player.getVelocity());
                callbackinfo.cancel();
            }
        }
    }

    public static void getBaseDimensions(Object object, CallbackInfoReturnable callbackinforeturnable) {
        if (object != VoilClient.mc.player) {
            Hitboxes hitboxes = (Hitboxes)ModuleManager.INSTANCE.getModule(Hitboxes.class);
            if (hitboxes != null && hitboxes.isEnabled() && Hitboxes.expandMode.is("Static")) {
                callbackinforeturnable.setReturnValue(EntityPose.STANDING);
            }
        }
    }

    public static float injectRotation(Object object, float f) {
        if (object != VoilClient.mc.player) {
            return f;
        }

        RotationManager rotationmanager = VoilClient.getRotationManager();
        return rotationmanager != null && rotationmanager.isActive() ? rotationmanager.getTargetRotation().getYawFloat() : f;
    }
}
