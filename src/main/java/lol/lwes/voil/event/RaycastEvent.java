package lol.lwes.voil.event;

import java.util.function.Predicate;
import net.minecraft.entity.Entity;

public class RaycastEvent {
    public final Entity entity;
    public boolean filtered;
    public Predicate<Entity> filter = entity1 -> true;

    private RaycastEvent(Entity entity) {
        this.entity = entity;
    }

    public static RaycastEvent create(Entity entity) {
        return new RaycastEvent(entity);
    }

    public boolean hasFilter() {
        return this.filtered;
    }

    public void setFilter(Predicate<Entity> predicate) {
        this.filtered = true;
        this.filter = predicate;
    }

    public boolean test(Entity entity) {
        return !this.filtered || this.filter.test(entity);
    }

    public Entity getEntity() {
        return this.entity;
    }
}
