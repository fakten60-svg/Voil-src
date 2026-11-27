package lol.lwes.voil.mixin;

import lol.lwes.voil.duck.ItemRenderCancelAccessor;
import lol.lwes.voil.util.render.Render2DUtil;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.render.item.KeyedItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin implements ItemRenderCancelAccessor {
    @Unique
    private boolean shouldCancel;
    @Unique
    private final Object bufferObject = new Object();
    @Shadow
    @Final
    private GuiRenderState state;

    @Inject(method = "prepareItemInitially", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V"))
    private void onPrepareItemInitially(KeyedItemRenderState keyeditemrenderstate, MatrixStack matrixstack, int i, int j, int k, CallbackInfo callbackinfo) {
    }

    @Inject(
        method = "prepareItemElements",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/state/GuiRenderState;forEachItemElement(Ljava/util/function/Consumer;)V")
    )
    private void prepIE_foreachitem(CallbackInfo callbackinfo) {
        Render2DUtil.prepareItemAtlas();
    }

    @Inject(method = "onItemAtlasChanged", at = @At("HEAD"))
    private void onItemAtlasChange(CallbackInfo callbackinfo) {
        Render2DUtil.clearItemCaches();
    }

    @Override
    public void tulip_setCancelItemRendering(boolean flag) {
        this.shouldCancel = flag;
    }

    @Inject(
        method = "prepareItemElements",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/state/GuiRenderState;getItemModelKeys()Ljava/util/Set;")
    )
    private void prepIE_pre(CallbackInfo callbackinfo) {
        this.state.getItemModelKeys().add(this.bufferObject);
    }

    @Inject(method = "prepareItemElements", at = @At(value = "INVOKE", target = "Ljava/util/Set;isEmpty()Z"))
    private void prepIE_post(CallbackInfo callbackinfo) {
        if (Render2DUtil.pendingItems.isEmpty()) {
            this.state.getItemModelKeys().remove(this.bufferObject);
        }
    }
}
