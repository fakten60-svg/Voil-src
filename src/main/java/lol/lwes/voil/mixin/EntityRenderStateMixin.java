package lol.lwes.voil.mixin;

import lol.lwes.voil.duck.EntityHolderAccessor;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateMixin implements EntityHolderAccessor {
    private Entity livingEntity;

    @Override
    public Entity tulip_getEntity() {
        return this.livingEntity;
    }

    @Override
    public void tulip_setEntity(Entity entity) {
        this.livingEntity = entity;
    }
}
