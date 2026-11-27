package dev.prestigeclient.renderlib.impl.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.prestigeclient.renderlib.impl.render.outline.CommandCaptureTracker;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl.ModelCommand;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl.ModelPartCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BatchingRenderCommandQueue.class)
public abstract class BatchingRenderCommandQueueMixin {
    @ModifyExpressionValue(method = "submitModel", at = @At(value = "NEW", target = "net/minecraft/client/render/command/OrderedRenderCommandQueueImpl$ModelCommand"))
    private ModelCommand recordModel(ModelCommand modelcommand) {
        CommandCaptureTracker.onCommand(modelcommand);
        return modelcommand;
    }

    @ModifyExpressionValue(method = "submitModelPart", at = @At(value = "NEW", target = "net/minecraft/client/render/command/OrderedRenderCommandQueueImpl$ModelPartCommand"))
    private ModelPartCommand recordModelPart(ModelPartCommand modelpartcommand) {
        CommandCaptureTracker.onCommand(modelpartcommand);
        return modelpartcommand;
    }
}
