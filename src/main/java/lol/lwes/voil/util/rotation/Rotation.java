package lol.lwes.voil.util.rotation;

import java.util.Arrays;
import lol.lwes.voil.VoilClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Rotation {
    private final double yaw;
    private final double yawOffset;
    private final double pitch;

    public Rotation(double d0, double d1) {
        this.yaw = MathHelper.wrapDegrees(d0);
        this.yawOffset = d0 - this.yaw;
        this.pitch = clampPitch(d1);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Rotation rotation1 && rotation1.yaw == this.yaw && rotation1.pitch == this.pitch;
    }

    @Override
    public String toString() {
        double d0 = Math.round(this.getPitch() * 100.0) / 100.0;
        double d1 = Math.round(this.getYaw() * 100.0) / 100.0;
        return "y:" + d1 + " p:" + d0;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new double[]{this.yaw, this.pitch});
    }

    public double getPitch() {
        return clampPitch(this.pitch);
    }

    public double yawDifference(Rotation rotation1) {
        double d0 = this.yaw;
        return angleDifference(d0, rotation1.yaw);
    }

    public float getYawFloat() {
        return (float)(this.yaw + this.yawOffset);
    }

    public static double clampPitch(double d0) {
        return MathHelper.clamp(d0, -90.0, 90.0);
    }

    public static Rotation applyMouseSensitivity(Rotation rotation, Rotation rotation1) {
        double d0 = VoilClient.mc.options.getMouseSensitivity().getValue() * 0.6F + 0.2F;
        double d1 = d0 * d0 * d0 * 8.0;
        Rotation rotation2 = rotation1.subtract(rotation);
        rotation2 = new Rotation(rotation2.getYaw() / 0.15, rotation2.pitch / 0.15);
        rotation2 = new Rotation(rotation2.getYaw() / d1, rotation2.pitch / d1);
        rotation2 = new Rotation(Math.round(rotation2.getYaw()), Math.round(rotation2.pitch));
        rotation2 = new Rotation(rotation2.getYaw() * d1, rotation2.pitch * d1);
        rotation2 = new Rotation(rotation2.getYaw() * 0.15, rotation2.pitch * 0.15);
        return rotation.add(rotation2);
    }

    public float getPitchFloat() {
        return (float)clampPitch(this.pitch);
    }

    private static double lerpAngle(double d0, double d1, double d2) {
        double d3 = MathHelper.wrapDegrees(d2 - d1);
        return MathHelper.wrapDegrees(d1 + d0 * d3);
    }

    public boolean isWithin(Rotation rotation1, double d0) {
        return Math.abs(this.yawDifference(rotation1)) <= d0 && Math.abs(this.pitchDifference(rotation1)) <= d0;
    }

    public Rotation add(Rotation rotation1) {
        return new Rotation(this.yawOffset + this.yaw + rotation1.yaw, this.pitch + rotation1.pitch);
    }

    public static Rotation fromEntity(Entity entity) {
        return entity == null ? new Rotation(0.0, 0.0) : new Rotation(entity.getYaw(), entity.getPitch());
    }

    public double getYaw() {
        return this.yaw + this.yawOffset;
    }

    public double getWrappedYaw() {
        return this.yawOffset;
    }

    public Rotation lerp(double d0, Rotation rotation1) {
        return new Rotation(
            this.yawOffset + lerpAngle(d0, this.yaw + this.yawOffset, rotation1.yaw + rotation1.yawOffset), MathHelper.lerp(d0, this.pitch, rotation1.pitch)
        );
    }

    public static double angleDifference(double d0, double d1) {
        double d2 = ((d1 - d0) % 360.0 + 360.0) % 360.0;
        return d2 > 180.0 ? d2 - 360.0 : d2;
    }

    public static Rotation fromPositions(Vec3d vec3d, Vec3d vec3d1) {
        return fromVectorSmoothed(vec3d1.subtract(vec3d));
    }

    public void applyTo(Entity entity) {
        entity.setYaw(this.getYawFloat());
        entity.setPitch(this.getPitchFloat());
    }

    public static double yawFromVector(Vec3d vec3d) {
        return Math.toDegrees(Math.atan2(vec3d.z, vec3d.x)) - 90.0;
    }

    public Rotation subtract(Rotation rotation1) {
        return new Rotation(this.yawOffset + this.yaw - rotation1.yaw, this.pitch - rotation1.pitch);
    }

    public static double angularDistance(Rotation rotation, Rotation rotation1) {
        double d0 = MathHelper.wrapDegrees(rotation.yaw);
        double d1 = MathHelper.wrapDegrees(rotation.pitch);
        double d2 = MathHelper.wrapDegrees(d0 - rotation1.yaw);
        double d3 = MathHelper.wrapDegrees(d1 - rotation1.pitch);
        return Math.sqrt(d2 * d2 + d3 * d3);
    }

    public double pitchDifference(Rotation rotation1) {
        double d0 = this.pitch;
        return angleDifference(d0, rotation1.pitch);
    }

    public static Rotation fromVectorSmoothed(Vec3d vec3d) {
        RotationManager rotationmanager = VoilClient.getRotationManager();
        Rotation rotation1;
        if (rotationmanager.isActive()) {
            rotation1 = rotationmanager.getTargetRotation();
        } else {
            ClientPlayerEntity clientplayerentity = VoilClient.mc.player;
            rotation1 = fromEntity(clientplayerentity);
        }

        Rotation rotation = new Rotation(yawFromVector(vec3d), pitchFromComponents(vec3d.y, vec3d.horizontalLength()));
        return applyMouseSensitivity(rotation1, rotation);
    }

    public static double yawFromComponents(double d0, double d1) {
        return Math.toDegrees(Math.atan2(d1, d0)) - 90.0;
    }

    public static Rotation lookAtPoint(Vec3d vec3d) {
        return fromPositions(VoilClient.mc.player.getEyePos(), vec3d);
    }

    public static double pitchFromComponents(Double d0, Double d1) {
        return -Math.toDegrees(Math.atan2(d0, d1));
    }

    public static Rotation toPoint(Vec3d vec3d) {
        Vec3d vec3d1 = vec3d.subtract(VoilClient.mc.player.getEyePos());
        return new Rotation(yawFromVector(vec3d1), pitchFromComponents(vec3d1.y, vec3d1.horizontalLength()));
    }

    public static Rotation fromMouseDelta(float f, float f1) {
        double d0 = VoilClient.mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2;
        double d1 = d0 * d0 * d0 * 1.2;
        if (d1 > 1.0E-6) {
            f = (float)(Math.round(f / d1) * d1);
            f1 = (float)(Math.round(f1 / d1) * d1);
        }

        return new Rotation(f, f1);
    }
}
