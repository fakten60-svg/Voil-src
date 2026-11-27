package lol.lwes.voil.hook;

import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.render.NoRender;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class InGameOverlayRendererHooks {
    public static void onRenderFireOverlay(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Sprite sprite, CallbackInfo callbackInfo) {
        NoRender noRender = (NoRender) ModuleManager.INSTANCE.getModule(NoRender.class);
        if (noRender != null && noRender.isEnabled() && noRender.fireOverlay.isEnabled()) {
            callbackInfo.cancel();
        }
    }
}
