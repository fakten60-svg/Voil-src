package lol.lwes.voil.event;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public class DamageEvent extends Event {
    public LivingEntity entity;
    public DamageSource source;

    public DamageEvent(LivingEntity livingentity, DamageSource damagesource) {
        this.entity = livingentity;
        this.source = damagesource;
    }
}
