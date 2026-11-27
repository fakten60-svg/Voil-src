package dev.prestigeclient.renderlib.impl.mixin;

import dev.prestigeclient.renderlib.api.entity.EntityOutlineHandler;
import dev.prestigeclient.renderlib.impl.render.outline.CommandCaptureTracker;
import dev.prestigeclient.renderlib.impl.render.outline.OutlineSeed;
import dev.prestigeclient.renderlib.impl.render.outline.OutlineUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl.ModelCommand;
import net.minecraft.client.render.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ModelCommandRenderer.class)
public abstract class OutlineSupplyingMixin {
    private VertexConsumer redirectOutlineLayer(VertexConsumer vertexconsumer, ModelCommand modelcommand, RenderLayer renderlayer) {
        if (EntityOutlineHandler.getActiveShader() == null) {
            return vertexconsumer;
        }

        if ((renderlayer.isOutline() || renderlayer.getAffectedOutline().isPresent())
            && modelcommand.state() instanceof EntityRenderState entityrenderstate
            && EntityOutlineHandler.shouldOutline(entityrenderstate)) {
            int i = OutlineUtils.textureIdOf(renderlayer);
            if (i >= 0) {
                OutlineSeed.set(entityrenderstate);
                vertexconsumer = VertexConsumers.union(vertexconsumer, OutlineUtils.outlineTee(i, modelcommand.sprite()));
            }
        }

        if (CommandCaptureTracker.isHandCommand(modelcommand) && EntityOutlineHandler.isTargetEntity(MinecraftClient.getInstance().player)) {
            int j = OutlineUtils.textureIdOf(renderlayer);
            if (j >= 0) {
                vertexconsumer = VertexConsumers.union(vertexconsumer, OutlineUtils.outlineTee(j, modelcommand.sprite()));
            }
        }

        return vertexconsumer;
    }
}
