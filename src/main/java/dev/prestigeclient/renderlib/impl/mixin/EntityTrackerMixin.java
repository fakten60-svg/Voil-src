package dev.prestigeclient.renderlib.impl.mixin;

import dev.prestigeclient.renderlib.api.entity.EntityOutlineHandler;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityTrackerMixin {
    @Inject(method = "updateRenderState", at = @At("RETURN"))
    private void trackEntity(Entity entity, EntityRenderState entityrenderstate, float f, CallbackInfo callbackinfo) {
        EntityOutlineHandler.trackEntity(entityrenderstate, entity);
    }
}
