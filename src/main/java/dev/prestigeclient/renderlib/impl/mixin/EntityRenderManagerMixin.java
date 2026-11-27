package dev.prestigeclient.renderlib.impl.mixin;

import java.util.Map;
import net.minecraft.client.render.entity.EntityRenderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityRenderManager.class)
public interface EntityRenderManagerMixin {
    @Accessor("playerRenderers")
    Map getPlayerRenderers();
}
