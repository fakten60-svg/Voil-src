package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.HeldItemRendererHooks;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
    @ModifyVariable(
        method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
        at = @At("STORE"),
        index = 6
    )
    private float modifySwingProgress(float f) {
        return HeldItemRendererHooks.modifySwingProgress(f);
    }

    @Inject(
        method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
        at = @At("HEAD")
    )
    private void renderFirstPersonItem(
        float f,
        MatrixStack matrixstack,
        OrderedRenderCommandQueue orderedrendercommandqueue,
        ClientPlayerEntity clientplayerentity,
        int i,
        CallbackInfo callbackinfo
    ) {
        HeldItemRendererHooks.onRenderFirstPersonItem();
    }

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
    public void onRenderItemStart(
        AbstractClientPlayerEntity abstractclientplayerentity,
        float f,
        float f1,
        Hand hand,
        float f2,
        ItemStack itemstack,
        float f3,
        MatrixStack matrixstack,
        OrderedRenderCommandQueue orderedrendercommandqueue,
        int i,
        CallbackInfo callbackinfo
    ) {
        HeldItemRendererHooks.onRenderItemStart(abstractclientplayerentity, f, f1, hand, f2, itemstack, f3, matrixstack, i, callbackinfo);
    }

    @ModifyVariable(method = "renderFirstPersonItem", at = @At("HEAD"), argsOnly = true, index = 7)
    private float modifyEquipProgress(float f) {
        return HeldItemRendererHooks.modifyEquipProgress();
    }

    @Inject(method = "renderFirstPersonItem", at = @At("RETURN"))
    public void onRenderItemEnd(
        AbstractClientPlayerEntity abstractclientplayerentity,
        float f,
        float f1,
        Hand hand,
        float f2,
        ItemStack itemstack,
        float f3,
        MatrixStack matrixstack,
        OrderedRenderCommandQueue orderedrendercommandqueue,
        int i,
        CallbackInfo callbackinfo
    ) {
        HeldItemRendererHooks.onRenderItemEnd(itemstack, matrixstack);
    }

    @Inject(
        method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
        at = @At("RETURN")
    )
    private void TAIL(
        float f,
        MatrixStack matrixstack,
        OrderedRenderCommandQueue orderedrendercommandqueue,
        ClientPlayerEntity clientplayerentity,
        int i,
        CallbackInfo callbackinfo
    ) {
        HeldItemRendererHooks.onRenderItemTail();
    }

    @Inject(
        method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
        at = @At("RETURN")
    )
    public void restoreHandRotation(
        float f,
        MatrixStack matrixstack,
        OrderedRenderCommandQueue orderedrendercommandqueue,
        ClientPlayerEntity clientplayerentity,
        int i,
        CallbackInfo callbackinfo
    ) {
        HeldItemRendererHooks.restoreHandRotation(clientplayerentity);
    }

    @Inject(
        method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
        at = @At("HEAD")
    )
    public void saveHandRotation(
        float f,
        MatrixStack matrixstack,
        OrderedRenderCommandQueue orderedrendercommandqueue,
        ClientPlayerEntity clientplayerentity,
        int i,
        CallbackInfo callbackinfo
    ) {
        HeldItemRendererHooks.saveHandRotation(f, clientplayerentity);
    }
}
