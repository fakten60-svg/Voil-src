package lol.lwes.voil.module.movement;

import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.ModeSetting;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import lol.lwes.voil.util.MovementUtil;
import lol.lwes.voil.util.Stopwatch;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.vehicle.BoatEntity;

public class Speed extends Module {
    ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Mode of the Speed", new String[]{"Vulcan", "NCP", "Grim", "Verus"});
    NumberSetting speed = new NumberSetting("Speed", 0.1, 1.0, 0.8, 0.1, "Speed of the Speed");
    private int airTicks = 0;
    private int groundTicks;
    private int boostTicks;
    Stopwatch timer = new Stopwatch();
    boolean jumped = false;

    public Speed() {
        super("Speed", "Makes you move faster", Category.Movement);
        Setting[] asetting = new Setting[]{this.mode, this.speed};
        this.addSettings(asetting);
    }

    @Override
    public void onTick() {
        if (this.mode.is("Vulcan")) {
            mc.options.jumpKey.setPressed(false);
            if (mc.player.isOnGround() && MovementUtil.hasMovementInput()) {
                this.groundTicks = 0;
                mc.player.jump();
                MovementUtil.strafeAtCurrentSpeed();
                if (MovementUtil.getHorizontalSpeed() < 0.5F) {
                    MovementUtil.strafe(this.speed.getFloat());
                }
            }

            if (!mc.player.isOnGround()) {
                int j = this.groundTicks++;
            }

            if (this.groundTicks == 4) {
                mc.player.setVelocity(mc.player.getVelocity().getX(), mc.player.getVelocity().getY() - 0.17, mc.player.getVelocity().getZ());
            }

            if (this.groundTicks == 1) {
                MovementUtil.strafe(0.33F);
            }
        } else if (this.mode.is("Grim") && MovementUtil.hasMovementInput()) {
            int i = 0;

            for (Entity entity : mc.world.getEntities()) {
                if (entity != mc.player
                    && (entity instanceof LivingEntity || entity instanceof BoatEntity)
                    && mc.player.getBoundingBox().expand(0.5).intersects(entity.getBoundingBox())) {
                    i++;
                }
            }

            double[] adouble = MovementUtil.getMoveOffset(0.08 * i);
            mc.player.addVelocity(adouble[0], 0.0, adouble[1]);
        } else if (this.mode.is("NCP")) {
            if (mc.player.isOnGround()) {
                mc.player.jump();
                MovementUtil.strafe(0.485F);
            } else if (mc.player.fallDistance > 0.6 && mc.player.fallDistance < 1.0) {
                MovementUtil.setMotionY(-0.8);
            } else {
                MovementUtil.strafe(0.25F);
            }
        } else if (this.mode.is("Verus")) {
            if (mc.player.isOnGround()) {
                mc.player.jump();
                MovementUtil.strafe(0.45F);
            } else {
                MovementUtil.strafe(0.28F);
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.airTicks = 0;
        this.groundTicks = 0;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.airTicks = 0;
        this.groundTicks = 0;
        this.groundTicks = 0;
        this.boostTicks = 0;
    }
}
