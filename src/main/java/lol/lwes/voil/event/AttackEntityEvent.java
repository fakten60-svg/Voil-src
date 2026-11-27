package lol.lwes.voil.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class AttackEntityEvent extends Event {
    public Entity target;
    public PlayerEntity player;

    public Entity getTarget() {
        return this.target;
    }

    public PlayerEntity getPlayer() {
        return this.player;
    }
}
