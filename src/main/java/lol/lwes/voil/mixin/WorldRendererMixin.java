package lol.lwes.voil.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import lol.lwes.voil.hook.WorldRenderHook;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.memory.ObjectAllocator;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/DefaultFramebufferSet;clear()V"))
    void postWR(
        ObjectAllocator objectallocator,
        RenderTickCounter rendertickcounter,
        boolean flag,
        Camera camera,
        Matrix4f matrix4f1,
        Matrix4f matrix4f,
        Matrix4f matrix4f2,
        GpuBufferSlice gpubufferslice,
        Vector4f vector4f,
        boolean flag1,
        CallbackInfo callbackinfo
    ) {
        WorldRenderHook.onRenderWorld(matrix4f);
    }
}
