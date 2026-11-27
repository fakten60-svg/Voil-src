package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class FireworkRocketEntityHooks {
    public static Vec3d fireworkMoveFix(LivingEntity livingentity) {
        return VoilClient.getRotationManager().isActive()
            ? VoilClient.mc
                .player
                .getRotationVector(
                    VoilClient.getRotationManager().getTargetRotation().getPitchFloat(), VoilClient.getRotationManager().getTargetRotation().getYawFloat()
                )
            : livingentity.getRotationVector();
    }
}
