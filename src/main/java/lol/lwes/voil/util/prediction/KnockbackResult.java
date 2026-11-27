package lol.lwes.voil.util.prediction;

import java.util.Objects;
import net.minecraft.util.math.Vec3d;

public record KnockbackResult(Vec3d position, double distance, boolean collided) {
    @Override
    public final boolean equals(Object object) {
        return this == object
            || object instanceof KnockbackResult knockbackresult1
                && this.collided == knockbackresult1.collided
                && Double.compare(this.distance, knockbackresult1.distance) == 0
                && Objects.equals(this.position, knockbackresult1.position);
    }

    @Override
    public final String toString() {
        return "bF[" + "cP=" + this.position + ", cQ=" + this.distance + ", cR=" + this.collided + "]";
    }

    @Override
    public final int hashCode() {
        int i = 0 + Objects.hashCode(this.position);
        i = i * 31 + Double.hashCode(this.distance);
        return i * 31 + Boolean.hashCode(this.collided);
    }
}
