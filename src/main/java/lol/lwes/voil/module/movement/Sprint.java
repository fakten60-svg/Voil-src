package lol.lwes.voil.module.movement;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.MovementUtil;
import lol.lwes.voil.util.rotation.Rotation;
import lol.lwes.voil.util.rotation.RotationPriority;

public class Sprint extends Module {
    private static boolean sprintBlocked = false;
    ModeSetting mode = new ModeSetting("Mode", "Smart", "Mode of the Sprint", new String[]{"Omni", "Rotation", "Smart"});

    public Sprint() {
        super("Sprint", "Makes you sprint", Category.Movement);
        Setting[] asetting = new Setting[]{this.mode};
        this.addSettings(asetting);
    }

    public static boolean isSprintBlocked() {
        return sprintBlocked;
    }

    @Override
    public void onTick() {
        if (this.isEnabled() && mc.player != null && mc.world != null) {
            if (sprintBlocked) {
                mc.player.setSprinting(false);
            } else {
                switch (this.mode.getMode()) {
                    case "Omni" -> {
                        if (MovementUtil.hasMovementInput()) {
                            mc.player.setSprinting(true);
                            MovementUtil.strafeAtCurrentSpeed();
                        }
                    }
                    case "Smart" -> {
                        if (mc.player.forwardSpeed > 0.0F) {
                            mc.player.setSprinting(true);
                        }
                    }
                    case "Rotation" -> {
                        if (MovementUtil.hasMovementInput()) {
                            mc.player.setSprinting(true);
                            float f = MovementUtil.getMovementYaw();
                            Rotation rotation = new Rotation(f, mc.player.getPitch());
                            VoilClient.getRotationManager().requestFromCaller(rotation, RotationPriority.LOW);
                        } else {
                            VoilClient.getRotationManager().reset();
                        }
                    }
                    default -> {
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        mc.player.setSprinting(false);
        VoilClient.getRotationManager().reset();
        sprintBlocked = false;
        super.onDisable();
    }

    public static void setSprintBlocked(boolean flag) {
        sprintBlocked = flag;
    }
}
