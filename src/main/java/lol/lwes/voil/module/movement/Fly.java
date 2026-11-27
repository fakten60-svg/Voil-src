package lol.lwes.voil.module.movement;

import lol.lwes.voil.event.EventHandler;
import lol.lwes.voil.event.TickEvent;
import lol.lwes.voil.module.Category;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.setting.NumberSetting;
import lol.lwes.voil.setting.Setting;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;

public class Fly extends Module {
    private final NumberSetting speed = new NumberSetting("Speed", 0.01, 50.0, 5.0, 1.0, "Movement speed of the boat");
    private final NumberSetting verticalSpeed = new NumberSetting("VerticalSpeed", 0.01, 5.0, 0.2, 0.01, "Vertical speed while flying");
    private final NumberSetting glideFactor = new NumberSetting("GlideFactor", 0.0, 1.0, 0.95, 0.01, "Glide factor for smoother movement");
    private double lastSpeed;

    public Fly() {
        super("Fly", "Allows you to fly with your boat", Category.Movement);
        Setting[] asetting = new Setting[]{this.speed, this.verticalSpeed, this.glideFactor};
        this.addSettings(asetting);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player != null && mc.player.getVehicle() instanceof BoatEntity) {
            ((BoatEntity)mc.player.getVehicle()).setVelocity(Vec3d.ZERO);
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void setEnabled(boolean flag) {
        if (!flag || mc.player != null && mc.player.getVehicle() instanceof BoatEntity) {
            if (mc.player != null) {
                this.lastSpeed = mc.player.getY();
            }

            super.setEnabled(flag);
        }
    }

    private void updateBoatMotion() {
        BoatEntity boatentity = (BoatEntity)mc.player.getVehicle();
        if (boatentity != null) {
            double d0 = this.speed.getFloat();
            double d1 = this.verticalSpeed.getFloat();
            double d2 = this.glideFactor.getFloat();
            Vec3d vec3d = boatentity.getVelocity();
            boolean flag = mc.options.forwardKey.isPressed();
            boolean flag1 = mc.options.backKey.isPressed();
            boolean flag2 = mc.options.leftKey.isPressed();
            boolean flag3 = mc.options.rightKey.isPressed();
            boolean flag4 = mc.options.jumpKey.isPressed();
            boolean flag5 = mc.options.sneakKey.isPressed();
            Vec3d vec3d1 = Vec3d.ZERO;
            if (flag) {
                vec3d1 = vec3d1.add(boatentity.getRotationVector().multiply(d0));
            }

            if (flag1) {
                vec3d1 = vec3d1.subtract(boatentity.getRotationVector().multiply(d0));
            }

            if (flag2) {
                vec3d1 = vec3d1.add(boatentity.getRotationVector().rotateY((float)Math.toRadians(90.0)).multiply(d0));
            }

            if (flag3) {
                vec3d1 = vec3d1.add(boatentity.getRotationVector().rotateY((float)Math.toRadians(-90.0)).multiply(d0));
            }

            if (flag4) {
                vec3d1 = vec3d1.add(0.0, d1, 0.0);
            }

            if (flag5) {
                vec3d1 = vec3d1.subtract(0.0, d1, 0.0);
            }

            Vec3d vec3d2 = vec3d.multiply(d2).add(vec3d1.multiply(1.0 - d2));
            boatentity.setVelocity(vec3d2);
        }
    }

    @EventHandler
    public void onUpdate(TickEvent tickevent) {
        if (mc.player != null && mc.player.getVehicle() instanceof BoatEntity) {
            this.updateBoatMotion();
        }
    }
}
