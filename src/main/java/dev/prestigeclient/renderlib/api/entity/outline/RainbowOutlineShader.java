package dev.prestigeclient.renderlib.api.entity.outline;

import dev.prestigeclient.renderlib.api.path.RLPath;
import dev.prestigeclient.renderlib.api.shader.RLEntityOutlineShader;
import dev.prestigeclient.renderlib.api.uniform.RLUniforms;
import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.InternalShaders;
import dev.prestigeclient.renderlib.impl.render.PostPipeline;
import dev.prestigeclient.renderlib.impl.render.layer.type.BufferCollector;
import dev.prestigeclient.renderlib.impl.render.layer.type.RenderLayer;
import dev.prestigeclient.renderlib.impl.render.mesh.GlBufferMeshBuilder;
import dev.prestigeclient.renderlib.impl.render.mgr.RenderPass;
import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import dev.prestigeclient.renderlib.impl.render.vertex.VertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class RainbowOutlineShader extends RLEntityOutlineShader {
    private final float rainbowSpeed = 0.5F;
    private final float saturation = 0.2F;
    private final float brightness = 1.0F;

    public RainbowOutlineShader() {
        super(RLPath.of("shaders/post.vert"), RLPath.of("shaders/rainbow_outline.frag"));
    }

    @Override
    public GlBufferMeshBuilder getBuffer(BufferCollector buffercollector, int i) {
        return buffercollector.getBuffer(
            outlineCache.computeIfAbsent(
                i,
                integer -> new RenderLayer(
                    VertexFormats.POSITION_COLOR_UV,
                    4,
                    true,
                    new StateApplier[]{
                        StateApplier.framebufferState(this.theFb.fbId),
                        StateApplier.shaderState(InternalShaders.POS_COLOR_UV),
                        StateApplier.flagState(2884, false, true),
                        StateApplier.blendState(true, true),
                        StateApplier.depthFuncState(519, 515),
                        StateApplier.apply(renderpass -> {
                            GL13.glActiveTexture(33984);
                            GL11.glBindTexture(3553, i);
                            bindMaskSampler();
                            RLUniforms.get(InternalShaders.POS_COLOR_UV.glId()).set1i("texSampler", 0);
                        })
                    }
                )
            )
        );
    }

    @Override
    public void applyUniforms(RLUniforms rluniforms) {
        rluniforms.set1f("time", (float)(System.currentTimeMillis() - time) / 1000.0F)
            .set1f("rainbowSpeed", 0.5F)
            .set1f("saturation", 0.2F)
            .set1f("brightness", 1.0F);
    }

    @Override
    public PostPipeline getPipeline() {
        return new PostPipeline().addPass(this.getProgram(), renderpass -> {
            GL13.glActiveTexture(33984);
            GL11.glBindTexture(3553, this.colTex.tex);
            this.applyUniforms();
        }, RenderAPI.getMinecraftFramebuffer());
    }
}
