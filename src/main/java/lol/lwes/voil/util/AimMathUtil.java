package lol.lwes.voil.util;

import java.util.concurrent.ThreadLocalRandom;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.util.rotation.Rotation;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AimMathUtil {
    private static float smoothedValue = Float.NaN;
    private static float smoothingFactor = 0.2F;

    public static double randomDouble(double d0, double d1) {
        return d0 >= d1 ? d0 : ThreadLocalRandom.current().nextDouble(d0, d1);
    }

    public static Vec3d addRandomJitter(Vec3d vec3d) {
        return new Vec3d(vec3d.x + randomDouble(-0.03, 0.03), vec3d.y + randomDouble(-0.03, 0.03), vec3d.z + randomDouble(-0.03, 0.03));
    }

    public static double wrapDegrees(double d0) {
        return ((d0 + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
    }

    public static int compareByDistance(Entity entity, Entity entity1) {
        return Float.compare(entity.distanceTo(VoilClient.mc.player), entity1.distanceTo(VoilClient.mc.player));
    }

    public static Vec3d getInterpolatedPos(Entity entity) {
        double d0 = SystemUtil.getTickDelta();
        return new Vec3d(
            MathHelper.lerp(d0, entity.lastX, entity.getX()),
            MathHelper.lerp(d0, entity.lastY, entity.getY()),
            MathHelper.lerp(d0, entity.lastZ, entity.getZ())
        );
    }

    public static void setSmoothingFactor(float f) {
        smoothingFactor = MathHelper.clamp(f, 0.0F, 1.0F);
    }

    public static double lerp(double d0, double d1, double d2) {
        return d0 + (d1 - d0) * d2;
    }

    public static float[] getRotationsToEntity(Entity entity) {
        double d0 = entity.getEntityPos().getX();
        double d1 = entity.getEntityPos().getY() + entity.getEyeHeight(entity.getPose());
        double d2 = entity.getEntityPos().getZ();
        return getRotationsTo(d0, d1, d2);
    }

    public static void setSmoothedValue(float f) {
        smoothedValue = f;
    }

    public static double clamp(double d0, double d1, double d2) {
        return Math.min(Math.max(d0, d1), d2);
    }

    public static boolean rollChance(double d0) {
        return Math.random() * 100.0 < d0;
    }

    public static Rotation getRotationTo(Entity entity, Vec3d vec3d) {
        double d0 = vec3d.x - entity.getX();
        double d1 = vec3d.y - entity.getY();
        double d2 = vec3d.z - entity.getZ();
        double d3 = MathHelper.sqrt((float)(d0 * d0 + d2 * d2));
        return new Rotation(
            (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(d2, d0)) - 90.0), (float)(-MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(d1, d3))))
        );
    }

    public static double distanceToBoxFromCamera(Box box) {
        return VoilClient.mc
            .player
            .getCameraPosVec(SystemUtil.getTickDelta())
            .distanceTo(closestPointOnBox(VoilClient.mc.player.getCameraPosVec(SystemUtil.getTickDelta()), box));
    }

    public static double interpolate(double d0, double d1, double d2) {
        return d0 + (d1 - d0) * d2;
    }

    public static float smooth(float f) {
        if (!Float.isFinite(smoothedValue)) {
            smoothedValue = f;
        }

        float f1 = MathHelper.clamp(smoothingFactor, 0.0F, 1.0F);
        smoothedValue = smoothedValue + (f - smoothedValue) * f1;
        return smoothedValue;
    }

    public static Vec3d closestPointOnBox(Vec3d vec3d, Box box) {
        return new Vec3d(clamp(vec3d.x, box.minX, box.maxX), clamp(vec3d.y, box.minY, box.maxY), clamp(vec3d.z, box.minZ, box.maxZ));
    }

    public static Vec3d closestPointOnBoxFromCamera(Box box) {
        return closestPointOnBox(VoilClient.mc.player.getCameraPosVec(SystemUtil.getTickDelta()), box);
    }

    public static Vec3d lerpVec(Vec3d vec3d, Vec3d vec3d1, double d0) {
        return new Vec3d(vec3d.x + (vec3d1.x - vec3d.x) * d0, vec3d.y + (vec3d1.y - vec3d.y) * d0, vec3d.z + (vec3d1.z - vec3d.z) * d0);
    }

    public static Vec3d closestPointOnEntity(Entity entity) {
        return closestPointOnBox(VoilClient.mc.player.getCameraPosVec(SystemUtil.getTickDelta()), getInterpolatedBox(entity));
    }

    public static float[] getRotationsTo(double d0, double d1, double d2) {
        double d3 = d0 - VoilClient.mc.player.getEntityPos().getX();
        double d4 = d1 - (VoilClient.mc.player.getEntityPos().getY() + VoilClient.mc.player.getEyeHeight(VoilClient.mc.player.getPose()));
        double d5 = d2 - VoilClient.mc.player.getEntityPos().getZ();
        double d6 = MathHelper.hypot(d3, d5);
        float f = (float)(Math.atan2(d5, d3) * 180.0 / Math.PI) - 90.0F;
        Object object = null;
        float f1 = (float)(-(Math.atan2(d4, d6) * 180.0 / Math.PI));
        float[] afloat = new float[]{f, f1};
        if (object != null) {
            ;
        }

        return afloat;
    }

    public static double distanceToBox(Vec3d vec3d, Box box) {
        return vec3d.distanceTo(closestPointOnBox(vec3d, box));
    }

    public static float smoothStep(float f, float f1, float f2) {
        f2 = MathHelper.clamp(f2, 0.0F, 1.0F);
        f2 = f2 * f2 * (3.0F - 2.0F * f2);
        return f + (f1 - f) * f2;
    }

    public static Box getInterpolatedBox(Entity entity) {
        return entity.getBoundingBox().offset(entity.getEntityPos().multiply(-1.0)).offset(getInterpolatedPos(entity));
    }

    public static boolean isWithinFov(Entity entity, double d0) {
        double d1 = d0 * 0.5;
        if (entity == null) {
            return false;
        }

        float f = VoilClient.mc.player.getYaw();
        float f1 = getRotationsToEntity(entity)[0];
        float f2 = f;
        double d2 = angleDelta(f2, f1);
        return d2 > 0.0 && d2 < d1 || -d1 < d2 && d2 < 0.0 && entity != null;
    }

    public static float angleDifference(float f, float f1) {
        return Math.abs(((f1 - f + 180.0F) % 360.0F + 360.0F) % 360.0F - 180.0F);
    }

    public static float lerpFloat(float f, float f1, float f2) {
        return f + (f1 - f) * f2;
    }

    public static float[] getRotationsToVec(Vec3d vec3d) {
        Vec3d vec3d1 = VoilClient.mc.player.getEyePos();
        double d0 = vec3d.x - vec3d1.x;
        double d1 = vec3d.y - vec3d1.y;
        double d2 = vec3d.z - vec3d1.z;
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        float f = (float)Math.toDegrees(Math.atan2(d2, d0)) - 90.0F;
        float f1 = (float)(-Math.toDegrees(Math.atan2(d1, d3)));
        return new float[]{MathHelper.wrapDegrees(f), MathHelper.wrapDegrees(f1)};
    }

    public static float angleDelta(float f, float f1) {
        float f2 = Math.abs(f1 - f) % 360.0F;
        Object object = null;
        float f3 = f2;
        if (object == null) {
            if (f2 > 180.0F) {
                f2 = 360.0F - f2;
            }

            f3 = f2;
        }

        return f3;
    }

    public static double getAngleDistanceTo(Vec3d vec3d) {
        float[] afloat = getRotationsToVec(vec3d);
        float f = MathHelper.wrapDegrees(VoilClient.mc.player.getYaw());
        float f1 = MathHelper.wrapDegrees(VoilClient.mc.player.getPitch());
        float f2 = f - afloat[0];
        float f3 = f1 - afloat[1];
        return Math.hypot(f2, f3);
    }

    public static float randomizedLerp(float f, float f1, float f2) {
        float f3 = f + (f1 - f) * f2;
        float f4 = (f1 - f) * 0.18F;
        double d0 = f3 + ThreadLocalRandom.current().nextGaussian() * f4;
        return (float)Math.max(f, Math.min(f1, d0));
    }

    public static int randomInt(int i, int j) {
        return i >= j ? i : ThreadLocalRandom.current().nextInt(i, j + 1);
    }
}
