package dev.prestigeclient.renderlib.impl.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.core.GlStateGuard;
import dev.prestigeclient.renderlib.impl.render.events.RenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.ProjectionMatrix3;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    @Final
    private GuiRenderer guiRenderer;
    @Shadow
    @Final
    private ProjectionMatrix3 hudProjectionMatrix;
    @Shadow
    @Final
    private Camera camera;

    @Inject(
        method = "render",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;guiRenderer:Lnet/minecraft/client/gui/render/GuiRenderer;", ordinal = 1)
    )
    void postBruh(RenderTickCounter rendertickcounter, boolean flag, CallbackInfo callbackinfo) {
        GlStateGuard glstateguard = GlStateGuard.capture();
        RenderAPI.runDelayedActions();
        Window window = this.client.getWindow();
        Matrix4f matrix4f = ((ProjectionMatrix2Accessor)((GuiRendererAccessor)this.guiRenderer)
            .getGuiProjectionMatrix())
            .callGetMatrix((float)window.getFramebufferWidth() / window.getScaleFactor(), (float)window.getFramebufferHeight() / window.getScaleFactor());
        RenderAPI.RenderContext renderapi$rendercontext = new RenderAPI.RenderContext(matrix4f, new Matrix4f().setTranslation(0.0F, 0.0F, -10000.0F), null);
        RenderAPI.drawHud(renderapi$rendercontext);
        glstateguard.restore();
    }

    @Inject(method = "renderWorld", at = @At("RETURN"))
    void afterWorld(RenderTickCounter rendertickcounter, CallbackInfo callbackinfo) {
        GlStateGuard glstateguard = GlStateGuard.capture();
        float f = rendertickcounter.getTickProgress(true);
        Matrix4f matrix4f = ((ProjectionMatrix3Accessor)this.hudProjectionMatrix)
            .callGetMatrix(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight(), this.getFov(this.camera, f, false));
        Matrix4f matrix4f1 = new Matrix4f(RenderSystem.getModelViewMatrix());
        Quaternionf quaternionf = this.camera.getRotation().conjugate(new Quaternionf());
        matrix4f1.rotate(quaternionf);
        RenderAPI.RenderContext renderapi$rendercontext = new RenderAPI.RenderContext(matrix4f, matrix4f1, null);
        RenderEvents.AFTER_WORLD.invoker().draw(RenderAPI.bufferCollector, renderapi$rendercontext);
        glstateguard.restore();
    }

    @Shadow
    abstract float getFov(Camera camera, float f, boolean flag);
}
