package lol.lwes.voil.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class AttackEntityPreEvent extends AttackEntityEvent {
    private static final AttackEntityPreEvent INSTANCE = new AttackEntityPreEvent();

    public static AttackEntityPreEvent create(PlayerEntity playerentity, Entity entity) {
        INSTANCE.setCancelled2(false);
        INSTANCE.player = playerentity;
        INSTANCE.target = entity;
        return INSTANCE;
    }
}
