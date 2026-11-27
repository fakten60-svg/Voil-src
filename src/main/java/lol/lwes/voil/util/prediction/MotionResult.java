package lol.lwes.voil.util.prediction;

import java.util.Objects;
import net.minecraft.util.math.Vec3d;

public record MotionResult(
    Vec3d position, Vec3d velocity, Vec3d impactPoint, double distance, int ticks, int hitCount, int groundTicks, boolean onGround, boolean collided
) {
    @Override
    public final boolean equals(Object object) {
        return this == object
            || object instanceof MotionResult motionresult1
                && this.collided == motionresult1.collided
                && this.onGround == motionresult1.onGround
                && this.groundTicks == motionresult1.groundTicks
                && this.hitCount == motionresult1.hitCount
                && this.ticks == motionresult1.ticks
                && Double.compare(this.distance, motionresult1.distance) == 0
                && Objects.equals(this.impactPoint, motionresult1.impactPoint)
                && Objects.equals(this.velocity, motionresult1.velocity)
                && Objects.equals(this.position, motionresult1.position);
    }

    @Override
    public final String toString() {
        return "bH["
            + "cV="
            + this.position
            + ", cW="
            + this.velocity
            + ", cX="
            + this.impactPoint
            + ", cY="
            + this.distance
            + ", cZ="
            + this.ticks
            + ", c0="
            + this.hitCount
            + ", c1="
            + this.groundTicks
            + ", c2="
            + this.onGround
            + ", c3="
            + this.collided
            + "]";
    }

    @Override
    public final int hashCode() {
        int i = 0 + Objects.hashCode(this.position);
        i = i * 31 + Objects.hashCode(this.velocity);
        i = i * 31 + Objects.hashCode(this.impactPoint);
        i = i * 31 + Double.hashCode(this.distance);
        i = i * 31 + Integer.hashCode(this.ticks);
        i = i * 31 + Integer.hashCode(this.hitCount);
        i = i * 31 + Integer.hashCode(this.groundTicks);
        i = i * 31 + Boolean.hashCode(this.onGround);
        return i * 31 + Boolean.hashCode(this.collided);
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public int getHitCount() {
        return this.hitCount;
    }

    public int getGroundTicks() {
        return this.groundTicks;
    }

    public boolean isCollided() {
        return this.collided;
    }

    public static int getMinTicks() {
        return 72;
    }

    public Vec3d getPosition() {
        return this.position;
    }

    public double getDistance() {
        return this.distance;
    }

    public int getTicks() {
        return this.ticks;
    }

    public Vec3d getImpactPoint() {
        return this.impactPoint;
    }

    public Vec3d getVelocity() {
        return this.velocity;
    }
}
