package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.RenderLayerHook;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.render.BlockRenderLayerGroup;
import net.minecraft.client.render.SectionRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SectionRenderState.class)
public abstract class SectionRenderStateMixin {
    @Inject(method = "renderSection", at = @At("HEAD"))
    private void afterSection(BlockRenderLayerGroup blockrenderlayergroup, GpuSampler gpusampler, CallbackInfo callbackinfo) {
        RenderLayerHook.renderCustomSky(blockrenderlayergroup);
    }
}
