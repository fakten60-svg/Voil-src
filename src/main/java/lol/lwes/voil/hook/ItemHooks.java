package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.RotationManager;
import net.minecraft.util.math.Vec3d;

public class ItemHooks {
    public static Vec3d modifyRaycastRotation(Vec3d original) {
        VoilClient client = VoilClient.getInstance();
        if (client == null || VoilClient.mc == null || VoilClient.mc.player == null) {
            return original;
        }

        RotationManager rotationManager = client.rotationManager;
        if (rotationManager == null || !rotationManager.isActive()) {
            return original;
        }

        Rotation rotation = rotationManager.getTargetRotation();
        return Vec3d.fromPolar(rotation.getPitchFloat(), rotation.getYawFloat());
    }
}
