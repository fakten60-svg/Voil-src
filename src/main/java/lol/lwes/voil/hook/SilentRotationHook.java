package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Unique;

public abstract class SilentRotationHook {
    @Unique
    private static float savedYaw;
    @Unique
    private static float savedPitch;
    @Unique
    private static float savedLastPitch;

    public static void applyRotation(LivingEntity livingentity) {
        if (shouldApplyRotation(livingentity)) {
            savedYaw = livingentity.getYaw();
            savedPitch = livingentity.getPitch();
            savedLastPitch = livingentity.lastPitch;
            VoilClient voilclient = VoilClient.getInstance();
            livingentity.setYaw(voilclient.rotationManager.getTargetRotation().getYawFloat());
            voilclient = VoilClient.getInstance();
            livingentity.setPitch(voilclient.rotationManager.getTargetRotation().getPitchFloat());
            voilclient = VoilClient.getInstance();
            livingentity.lastPitch = voilclient.rotationManager.getTargetRotation().getPitchFloat();
        }
    }

    public static void restoreRotation(LivingEntity livingentity) {
        if (shouldApplyRotation(livingentity) && savedYaw != 0.0F && savedPitch != 0.0F) {
            livingentity.setYaw(savedYaw);
            livingentity.setPitch(savedPitch);
            livingentity.lastPitch = savedLastPitch;
            savedYaw = 0.0F;
            savedPitch = 0.0F;
        }
    }

    @Unique
    private static boolean shouldApplyRotation(LivingEntity livingentity) {
        return livingentity == VoilClient.mc.player
            && VoilClient.getInstance().rotationManager.isActive()
            && !(VoilClient.mc.currentScreen instanceof InventoryScreen)
            && !(VoilClient.mc.currentScreen instanceof CreativeInventoryScreen);
    }
}
