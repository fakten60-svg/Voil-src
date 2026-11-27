package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.DamageEvent;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.render.SwingAnimation;
import lol.lwes.voil.util.rotation.RotationManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class LivingEntityHook {
    private static final String NO_DELAY_MODE = "No Delay";

    public static float overrideBodyYaw(Object object, float f) {
        if (object != VoilClient.mc.player) {
            return f;
        }

        RotationManager rotationmanager = VoilClient.getRotationManager();
        return rotationmanager.isActive() ? rotationmanager.getTargetRotation().getYawFloat() : f;
    }

    public static void onDamage(Object object, DamageSource damagesource) {
        if (!VoilClient.getInstance().disabled) {
            DamageEvent damageevent = new DamageEvent((LivingEntity)object, damagesource);
            VoilClient.eventBus.post(damageevent);
        }
    }

    public static float overrideHeadYaw(Object object, float f) {
        if (object != VoilClient.mc.player) {
            return f;
        }

        RotationManager rotationmanager = VoilClient.getRotationManager();
        return rotationmanager.isActive() ? rotationmanager.getTargetRotation().getYawFloat() : f;
    }

    public static Vec3d overrideRotationVector(Object object, Vec3d vec3d) {
        if (object != VoilClient.mc.player) {
            return vec3d;
        }

        RotationManager rotationmanager = VoilClient.getRotationManager();
        return rotationmanager.isActive()
            ? VoilClient.mc.player.getRotationVector(rotationmanager.getTargetRotation().getPitchFloat(), rotationmanager.getTargetRotation().getYawFloat())
            : vec3d;
    }

    public static void onGetHandSwingDuration(Object object, CallbackInfoReturnable callbackinforeturnable) {
        if (object == VoilClient.mc.player) {
            if (ModuleManager.INSTANCE.isEnabled(SwingAnimation.class) && NO_DELAY_MODE.equals(SwingAnimation.mode.getMode())) {
                callbackinforeturnable.setReturnValue(0);
            }
        }
    }
}
