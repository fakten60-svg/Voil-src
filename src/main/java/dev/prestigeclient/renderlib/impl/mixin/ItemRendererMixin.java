package dev.prestigeclient.renderlib.impl.mixin;

import dev.prestigeclient.renderlib.api.entity.EntityOutlineHandler;
import dev.prestigeclient.renderlib.impl.render.outline.OutlineSeed;
import dev.prestigeclient.renderlib.impl.render.outline.OutlineUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    private static VertexConsumer captureFirstPersonItem(VertexConsumer vertexconsumer, RenderLayer renderlayer, ItemDisplayContext itemdisplaycontext) {
        if (EntityOutlineHandler.getActiveShader() != null && itemdisplaycontext.isFirstPerson()) {
            MinecraftClient minecraftclient = MinecraftClient.getInstance();
            if (!EntityOutlineHandler.isTargetEntity(minecraftclient.player)) {
                return vertexconsumer;
            }

            int i = OutlineUtils.textureIdOf(renderlayer);
            if (i < 0) {
                return vertexconsumer;
            }

            OutlineSeed.set(minecraftclient.player);
            return VertexConsumers.union(vertexconsumer, OutlineUtils.outlineTee(i, null));
        } else {
            return vertexconsumer;
        }
    }
}
