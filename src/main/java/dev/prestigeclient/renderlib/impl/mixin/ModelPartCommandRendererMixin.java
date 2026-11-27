package dev.prestigeclient.renderlib.impl.mixin;

import dev.prestigeclient.renderlib.api.entity.EntityOutlineHandler;
import dev.prestigeclient.renderlib.impl.render.outline.CommandCaptureTracker;
import dev.prestigeclient.renderlib.impl.render.outline.OutlineUtils;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.command.ModelPartCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl.ModelPartCommand;
import net.minecraft.client.render.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ModelPartCommandRenderer.class)
public abstract class ModelPartCommandRendererMixin {
    private VertexConsumer captureModelPart(VertexConsumer vertexconsumer, RenderLayer renderlayer, ModelPartCommand modelpartcommand) {
        if (EntityOutlineHandler.getActiveShader() == null) {
            return vertexconsumer;
        }

        EntityRenderState entityrenderstate = CommandCaptureTracker.ownerOf(modelpartcommand);
        if (entityrenderstate == null && !CommandCaptureTracker.isHandCommand(modelpartcommand)) {
            return vertexconsumer;
        }

        int i = OutlineUtils.textureIdOf(renderlayer);
        return i < 0 ? vertexconsumer : VertexConsumers.union(vertexconsumer, OutlineUtils.outlineTee(i, modelpartcommand.sprite()));
    }
}
