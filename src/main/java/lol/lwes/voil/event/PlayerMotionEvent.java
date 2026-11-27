package lol.lwes.voil.event;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

public final class PlayerMotionEvent extends MotionEvent {
    private static final PlayerMotionEvent INSTANCE = new PlayerMotionEvent();
    private static float currentYaw;
    private static float prevYaw;
    private static float currentPitch;
    private static float prevPitch;
    private float yaw;
    private float pitch;
    private double x;
    private double y;
    private double z;
    private boolean onGround;

    public static float getCurrentYaw() {
        return currentYaw;
    }

    public void commitRotation() {
        prevYaw = currentYaw;
        prevPitch = currentPitch;
        currentYaw = this.yaw;
        currentPitch = this.pitch;
    }

    public void setYaw(float f) {
        MinecraftClient.getInstance().player.setBodyYaw(f);
        MinecraftClient.getInstance().player.headYaw = f;
        this.yaw = f;
    }

    public double getY() {
        return this.y;
    }

    public static float getPrevPitch() {
        return prevPitch;
    }

    public static float getPrevYaw() {
        return prevYaw;
    }

    public double getX() {
        return this.x;
    }

    public static PlayerMotionEvent create(float f, float f1, double d0, double d1, double d2, boolean flag) {
        INSTANCE.setCancelled2(false);
        INSTANCE.yaw = f;
        INSTANCE.pitch = f1;
        INSTANCE.x = d0;
        INSTANCE.y = d1;
        INSTANCE.z = d2;
        INSTANCE.onGround = flag;
        return INSTANCE;
    }

    public void setX(double d0) {
        this.x = d0;
    }

    public void setY(double d0) {
        this.y = d0;
    }

    public double getZ() {
        return this.z;
    }

    public void setOnGround(boolean flag) {
        this.onGround = flag;
    }

    public float getPitch() {
        return this.pitch;
    }

    public static float getCurrentPitch() {
        return currentPitch;
    }

    public void setPosition(double d0, double d1, double d2, boolean flag) {
        this.setX(d0);
        this.setY(d1);
        this.setZ(d2);
        this.setOnGround(flag);
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public void setZ(double d0) {
        this.z = d0;
    }

    public void setPitch(float f) {
        this.pitch = MathHelper.clamp(f, -90.0F, 90.0F);
    }

    public float getYaw() {
        return this.yaw;
    }
}
