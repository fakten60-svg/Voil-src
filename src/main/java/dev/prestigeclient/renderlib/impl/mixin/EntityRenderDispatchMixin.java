package dev.prestigeclient.renderlib.impl.mixin;

import dev.prestigeclient.renderlib.impl.render.outline.CommandCaptureTracker;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderManager.class)
public abstract class EntityRenderDispatchMixin {
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/EntityRenderManager;getRenderer(Lnet/minecraft/client/render/entity/state/EntityRenderState;)Lnet/minecraft/client/render/entity/EntityRenderer;"
        )
    )
    private void beginEntityWindow(
        EntityRenderState entityrenderstate,
        CameraRenderState camerarenderstate,
        double d0,
        double d1,
        double d2,
        MatrixStack matrixstack,
        OrderedRenderCommandQueue orderedrendercommandqueue,
        CallbackInfo callbackinfo
    ) {
        CommandCaptureTracker.beginEntity(entityrenderstate);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void endEntityWindow(
        EntityRenderState entityrenderstate,
        CameraRenderState camerarenderstate,
        double d0,
        double d1,
        double d2,
        MatrixStack matrixstack,
        OrderedRenderCommandQueue orderedrendercommandqueue,
        CallbackInfo callbackinfo
    ) {
        CommandCaptureTracker.endEntity();
    }
}
