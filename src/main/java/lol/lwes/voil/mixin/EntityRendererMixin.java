package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.EntityRendererHooks;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void renderLabelIfPresent(
        EntityRenderState entityrenderstate,
        MatrixStack matrixstack,
        OrderedRenderCommandQueue orderedrendercommandqueue,
        CameraRenderState camerarenderstate,
        CallbackInfo callbackinfo
    ) {
        EntityRendererHooks.renderLabelIfPresent();
    }

    @Inject(method = "updateRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getType()Lnet/minecraft/entity/EntityType;"))
    public void onUpdateRenderState(Entity entity, EntityRenderState entityrenderstate, float f, CallbackInfo callbackinfo) {
        EntityRendererHooks.onUpdateRenderState(entity, entityrenderstate);
    }
}
