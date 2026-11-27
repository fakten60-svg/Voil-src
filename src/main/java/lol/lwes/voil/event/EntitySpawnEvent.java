package lol.lwes.voil.event;

import net.minecraft.entity.Entity;

public class EntitySpawnEvent extends Event {
    private final Entity entity;

    public EntitySpawnEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return this.entity;
    }
}
