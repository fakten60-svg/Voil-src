package lol.lwes.voil.util.prediction;

import java.util.Objects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public record MotionRequest(LivingEntity entity, int ticks, Vec3d velocity) {
    private static final int DEFAULT_TICKS = 200;

    public MotionRequest(LivingEntity livingentity, int i) {
        this(livingentity, i, null);
    }

    public MotionRequest(LivingEntity livingentity) {
        this(livingentity, DEFAULT_TICKS, null);
    }

    @Override
    public final boolean equals(Object object) {
        return this == object
            || object instanceof MotionRequest motionrequest1
                && Objects.equals(this.velocity, motionrequest1.velocity)
                && this.ticks == motionrequest1.ticks
                && Objects.equals(this.entity, motionrequest1.entity);
    }

    @Override
    public final String toString() {
        return "bG[" + "cS=" + this.entity + ", cT=" + this.ticks + ", cU=" + this.velocity + "]";
    }

    @Override
    public final int hashCode() {
        int i = 0 + Objects.hashCode(this.entity);
        i = i * 31 + Integer.hashCode(this.ticks);
        return i * 31 + Objects.hashCode(this.velocity);
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    public Vec3d getVelocity() {
        return this.velocity;
    }

    public int getTicks() {
        return this.ticks;
    }
}
