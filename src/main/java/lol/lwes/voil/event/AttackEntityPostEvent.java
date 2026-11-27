package lol.lwes.voil.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class AttackEntityPostEvent extends AttackEntityEvent {
    private static final AttackEntityPostEvent INSTANCE = new AttackEntityPostEvent();

    public static AttackEntityPostEvent create(PlayerEntity playerentity, Entity entity) {
        INSTANCE.setCancelled2(false);
        INSTANCE.player = playerentity;
        INSTANCE.target = entity;
        return INSTANCE;
    }
}
