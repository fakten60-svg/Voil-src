package lol.lwes.voil.util;

import lol.lwes.voil.VoilClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class MovementUtil {
    public static double addVelocityY(double d0) {
        Vec3d vec3d = VoilClient.mc.player.getVelocity();
        VoilClient.mc.player.setVelocity(vec3d.x, vec3d.y + d0, vec3d.z);
        return d0;
    }

    public static float getHorizontalSpeed() {
        return (float)Math.sqrt(
            VoilClient.mc.player.getVelocity().x * VoilClient.mc.player.getVelocity().x
                + VoilClient.mc.player.getVelocity().z * VoilClient.mc.player.getVelocity().z
        );
    }

    public static Vec3d getLastPos(Entity entity) {
        return new Vec3d(entity.lastX, entity.lastY, entity.lastZ);
    }

    private static double getMoveDirection() {
        MinecraftClient minecraftclient = MinecraftClient.getInstance();
        if (minecraftclient.player == null) {
            return 0.0;
        }

        double d0 = minecraftclient.player.getYaw();
        float f = minecraftclient.player.forwardSpeed;
        float f1 = minecraftclient.player.sidewaysSpeed;
        if (f < 0.0F) {
            d0 += 180.0;
        }

        double d1 = 1.0;
        if (f < 0.0F) {
            d1 = -0.5;
        } else if (f > 0.0F) {
            d1 = 0.5;
        }

        if (f1 > 0.0F) {
            d0 -= 90.0 * d1;
        }

        if (f1 < 0.0F) {
            d0 += 90.0 * d1;
        }

        return Math.toRadians(d0);
    }

    public static boolean hasVelocity() {
        Vec3d vec3d = VoilClient.mc.player.getVelocity();
        return vec3d.getX() != 0.0 || vec3d.getY() != 0.0 || vec3d.getZ() != 0.0;
    }

    public static boolean hasMovementInput() {
        PlayerInput playerinput = VoilClient.mc.player.input.playerInput;
        return playerinput.forward() || playerinput.backward() || playerinput.left() || playerinput.right();
    }

    public static float getMovementYaw() {
        float f = VoilClient.mc.player.forwardSpeed;
        float f1 = VoilClient.mc.player.sidewaysSpeed;
        float f2 = VoilClient.mc.player.getYaw();
        if (f == 0.0F && f1 == 0.0F) {
            return f2;
        }

        double d0 = f;
        return (float)Math.toDegrees(getDirectionRadians(f2, d0, f1));
    }

    public static double getGroundDistance() {
        double d0 = VoilClient.mc.player.getX();
        int i = (int)Math.floor(VoilClient.mc.player.getY());
        double d1 = VoilClient.mc.player.getZ();

        for (int j = i; j > 0; j--) {
            BlockPos blockpos = new BlockPos((int)d0, j, (int)d1);
            if (!VoilClient.mc.world.isAir(blockpos)) {
                return i - j;
            }
        }

        return 0.0;
    }

    public static boolean isMovementKeyPressed() {
        return VoilClient.mc.options.forwardKey.isPressed()
            || VoilClient.mc.options.backKey.isPressed()
            || VoilClient.mc.options.leftKey.isPressed()
            || VoilClient.mc.options.rightKey.isPressed();
    }

    public static double setVelocityY(double d0) {
        Vec3d vec3d = VoilClient.mc.player.getVelocity();
        VoilClient.mc.player.setVelocity(vec3d.x, d0, vec3d.z);
        return d0;
    }

    public static Vec3d getPositionDelta(Entity entity) {
        return entity.getEntityPos().subtract(getLastPos(entity));
    }

    public static void setMotionY(double d0) {
        if (VoilClient.mc.player != null) {
            VoilClient.mc.player.setVelocity(VoilClient.mc.player.getVelocity().x, d0, VoilClient.mc.player.getVelocity().z);
        }
    }

    public static double getDirectionRadians(float f, double d0, double d1) {
        if (d0 < 0.0) {
            f += 180.0F;
        }

        float f1 = 1.0F;
        if (d0 < 0.0) {
            f1 = -0.5F;
        } else if (d0 > 0.0) {
            f1 = 0.5F;
        }

        if (d1 > 0.0) {
            f -= 90.0F * f1;
        }

        if (d1 < 0.0) {
            f += 90.0F * f1;
        }

        return Math.toRadians(f);
    }

    public static void stopHorizontal(boolean flag) {
        VoilClient.mc.player.setVelocity(0.0, VoilClient.mc.player.getVelocity().y, 0.0);
        if (flag) {
            VoilClient.mc.player.setVelocity(0.0, 0.0, 0.0);
        }
    }

    public static void strafeAtCurrentSpeed() {
        strafe(getHorizontalSpeed());
    }

    public static float getForwardSpeed() {
        return VoilClient.mc.player == null ? 0.0F : VoilClient.mc.player.forwardSpeed;
    }

    public static void setHorizontalSpeed(double d0) {
        if (VoilClient.mc.player.forwardSpeed != 0.0F || VoilClient.mc.player.sidewaysSpeed != 0.0F) {
            double d1 = Math.toRadians(VoilClient.mc.player.getYaw());
            double d2 = -Math.sin(d1) * d0;
            double d3 = Math.cos(d1) * d0;
            VoilClient.mc.player.setVelocity(d2, VoilClient.mc.player.getVelocity().y, d3);
        }
    }

    public static float resetAndSetSpeed(float f) {
        if (VoilClient.mc.player == null) {
            return 0.0F;
        }

        VoilClient.mc.player.setVelocity(VoilClient.mc.player.getVelocity().x, 0.0, VoilClient.mc.player.getVelocity().z);
        VoilClient.mc.player.setVelocity(f, 0.0, f);
        return getHorizontalSpeed();
    }

    public static double[] getMoveOffset(double d0) {
        if (VoilClient.mc.player == null) {
            return new double[]{0.0, 0.0};
        }

        double d1 = getMoveDirection();
        double d2 = -Math.sin(d1) * d0;
        double d3 = Math.cos(d1) * d0;
        return new double[]{d2, d3};
    }

    public static float getSidewaysSpeed() {
        return VoilClient.mc.player == null ? 0.0F : VoilClient.mc.player.sidewaysSpeed;
    }

    public static void strafe(float f) {
        if (hasMovementInput()) {
            double d0 = getMoveDirection();
            double d1 = -Math.sin(d0) * f;
            double d2 = Math.cos(d0) * f;
            Vec3d vec3d = new Vec3d(d1, VoilClient.mc.player.getVelocity().y, d2);
            VoilClient.mc.player.setVelocity(vec3d);
        }
    }
}
