package lol.lwes.voil.util.prediction;

import java.util.Objects;
import net.minecraft.entity.LivingEntity;

public record KnockbackRequest(LivingEntity attacker, LivingEntity target, float yaw, boolean sprinting, int ticks) {
    @Override
    public final boolean equals(Object object) {
        return this == object
            || object instanceof KnockbackRequest knockbackrequest1
                && this.ticks == knockbackrequest1.ticks
                && this.sprinting == knockbackrequest1.sprinting
                && Float.compare(this.yaw, knockbackrequest1.yaw) == 0
                && Objects.equals(this.target, knockbackrequest1.target)
                && Objects.equals(this.attacker, knockbackrequest1.attacker);
    }

    @Override
    public final String toString() {
        return "bE[" + "cK=" + this.attacker + ", cL=" + this.target + ", cM=" + this.yaw + ", cN=" + this.sprinting + ", cO=" + this.ticks + "]";
    }

    @Override
    public final int hashCode() {
        int i = 0 + Objects.hashCode(this.attacker);
        i = i * 31 + Objects.hashCode(this.target);
        i = i * 31 + Float.hashCode(this.yaw);
        i = i * 31 + Boolean.hashCode(this.sprinting);
        return i * 31 + Integer.hashCode(this.ticks);
    }
}
