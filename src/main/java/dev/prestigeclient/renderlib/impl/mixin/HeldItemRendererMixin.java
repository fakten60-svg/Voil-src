package dev.prestigeclient.renderlib.impl.mixin;

import dev.prestigeclient.renderlib.impl.render.outline.CommandCaptureTracker;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
    @Inject(
        method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
        at = @At("HEAD")
    )
    private void beginHandWindow(
        float f,
        MatrixStack matrixstack,
        OrderedRenderCommandQueue orderedrendercommandqueue,
        ClientPlayerEntity clientplayerentity,
        int i,
        CallbackInfo callbackinfo
    ) {
        CommandCaptureTracker.beginHand();
    }

    @Inject(
        method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
        at = @At("RETURN")
    )
    private void endHandWindow(
        float f,
        MatrixStack matrixstack,
        OrderedRenderCommandQueue orderedrendercommandqueue,
        ClientPlayerEntity clientplayerentity,
        int i,
        CallbackInfo callbackinfo
    ) {
        CommandCaptureTracker.endHand();
    }
}
